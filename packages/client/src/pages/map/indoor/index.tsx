import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../../components/PageShell'
import api, {
  DEFAULT_PUBLIC_LOCALE,
  type PublicIndoorBuildingDto,
  type PublicIndoorFloorDto,
  type PublicIndoorRuntimeOverlayGeometryDto,
  type PublicIndoorRuntimeTriggeredEffectDto,
  type PublicLocaleCode,
} from '../../../services/api'
import {
  describeIndoorBlockedReason,
  fetchIndoorRuntimeFloorWithFallback,
  findIndoorRuntimeNode,
  getDefaultTriggerOption,
  getIndoorRuntimeSessionId,
  getRunnableTriggerOptions,
  submitIndoorRuntimeInteraction,
  type IndoorRuntimeFloorView,
  type IndoorRuntimeNodeView,
  type IndoorRuntimeTriggerOption,
} from '../../../services/indoorRuntime'
import { requireAuth } from '../../../services/gameService'
import './index.scss'

interface ParsedTile {
  z: number
  x: number
  y: number
  url: string
}

interface ParsedTileManifest {
  defaultLevel: number
  gridCols: number
  gridRows: number
  tiles: ParsedTile[]
}

interface OverlayBounds {
  left: number
  top: number
  width: number
  height: number
  geometryType?: string
  label?: string
}

interface EffectMessage {
  title: string
  body: string
  category: string
}

interface PathPoint {
  x: number
  y: number
}

interface ActivePathMotion {
  nodeId: number
  behaviorId: number
  label: string
  points: PathPoint[]
  durationMs: number
  holdMs: number
  loop: boolean
}

function toNumber(value?: string) {
  if (!value) {
    return undefined
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : undefined
}

function clamp(value: number, min: number, max: number) {
  return Math.min(Math.max(value, min), max)
}

function buildVirtualFloors(building: PublicIndoorBuildingDto | null): PublicIndoorFloorDto[] {
  if (!building) {
    return []
  }
  if (building.floors?.length) {
    return building.floors
  }
  const total = Math.max(1, building.totalFloors || 1)
  return Array.from({ length: total }, (_, index) => ({
    id: -(index + 1),
    floorCode: `F${index + 1}`,
    floorNumber: index + 1,
    name: `${index + 1}F`,
    zoomMin: 0.5,
    zoomMax: 2,
    defaultZoom: 1,
  }))
}

function floorDisplayName(floor?: PublicIndoorFloorDto | IndoorRuntimeFloorView | null) {
  if (!floor) {
    return ''
  }
  return floor.name || floor.floorCode || `${floor.floorNumber || ''}F`
}

function parseTileManifest(manifestJson?: string): ParsedTileManifest | null {
  if (!manifestJson) {
    return null
  }
  try {
    const parsed = JSON.parse(manifestJson) as {
      defaultLevel?: number
      gridCols?: number
      gridRows?: number
      tiles?: ParsedTile[]
    }
    if (!parsed.tiles?.length || !parsed.gridCols || !parsed.gridRows) {
      return null
    }
    return {
      defaultLevel: Number.isFinite(parsed.defaultLevel) ? Number(parsed.defaultLevel) : 0,
      gridCols: parsed.gridCols,
      gridRows: parsed.gridRows,
      tiles: parsed.tiles,
    }
  } catch (error) {
    console.warn('Failed to parse indoor tile manifest.', error)
    return null
  }
}

function overlayBounds(geometry?: PublicIndoorRuntimeOverlayGeometryDto | null): OverlayBounds | null {
  const points = (geometry?.points || []).filter(
    (point): point is { x: number; y: number } => typeof point.x === 'number' && typeof point.y === 'number',
  )
  if (!points.length) {
    return null
  }
  const xs = points.map((point) => point.x)
  const ys = points.map((point) => point.y)
  const left = Math.min(...xs)
  const right = Math.max(...xs)
  const top = Math.min(...ys)
  const bottom = Math.max(...ys)
  const properties = geometry?.properties || {}
  return {
    left,
    top,
    width: Math.max(right - left, 0.04),
    height: Math.max(bottom - top, 0.04),
    geometryType: geometry?.geometryType,
    label: typeof properties.label === 'string' ? properties.label : undefined,
  }
}

function pathPoints(effect?: PublicIndoorRuntimeTriggeredEffectDto | null): PathPoint[] {
  const candidates = effect?.pathGraph?.points || effect?.overlayGeometry?.points || []
  return candidates
    .filter((point): point is { x: number; y: number } => typeof point.x === 'number' && typeof point.y === 'number')
    .sort((left, right) => (left.order || 0) - (right.order || 0))
    .map((point) => ({ x: point.x, y: point.y }))
}

function effectMessage(effect: PublicIndoorRuntimeTriggeredEffectDto, fallbackTitle: string): EffectMessage {
  const config = effect.config || {}
  const title = typeof config.title === 'string' && config.title.trim()
    ? config.title
    : effect.label || fallbackTitle
  const body = typeof config.body === 'string' && config.body.trim()
    ? config.body
    : typeof config.description === 'string' && config.description.trim()
      ? config.description
      : '這個互動已經啟動。'
  return {
    title,
    body,
    category: effect.category || 'popup',
  }
}

function markerLabel(node?: IndoorRuntimeNodeView | null) {
  if (!node) {
    return ''
  }
  return node.name || node.markerCode || '節點'
}

function triggerButtonLabel(option: IndoorRuntimeTriggerOption) {
  if (option.category === 'dwell' && option.dwellSeconds) {
    return `停留 ${option.dwellSeconds} 秒`
  }
  if (option.category === 'proximity' && option.radiusMeters) {
    return `模擬靠近 ${option.radiusMeters}m`
  }
  if (option.category === 'tap') {
    return '點擊互動'
  }
  return option.label || '啟動互動'
}

function motionSegmentStyle(from: PathPoint, to: PathPoint) {
  const dx = to.x - from.x
  const dy = to.y - from.y
  const length = Math.sqrt(dx * dx + dy * dy)
  const angle = Math.atan2(dy, dx) * (180 / Math.PI)
  return {
    left: `${from.x * 100}%`,
    top: `${from.y * 100}%`,
    width: `${length * 100}%`,
    transform: `translateY(-50%) rotate(${angle}deg)`,
  }
}

export default function IndoorMapPage() {
  const router = Taro.getCurrentInstance().router
  const buildingId = toNumber(router?.params?.buildingId)
  const poiId = toNumber(router?.params?.poiId)
  const fallbackTitle = router?.params?.title || router?.params?.poiName || router?.params?.name || '室內地圖'

  const locale = (Taro.getStorageSync('locale') || DEFAULT_PUBLIC_LOCALE) as PublicLocaleCode

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [building, setBuilding] = useState<PublicIndoorBuildingDto | null>(null)
  const [activeFloorId, setActiveFloorId] = useState<number | null>(null)
  const [scale, setScale] = useState(1)
  const [floorCache, setFloorCache] = useState<Record<number, IndoorRuntimeFloorView>>({})
  const [floorLoadingId, setFloorLoadingId] = useState<number | null>(null)
  const [selectedNodeId, setSelectedNodeId] = useState<number | null>(null)
  const [runtimeNotice, setRuntimeNotice] = useState<string | null>(null)
  const [activePopup, setActivePopup] = useState<EffectMessage | null>(null)
  const [activeBubble, setActiveBubble] = useState<(EffectMessage & { nodeId: number }) | null>(null)
  const [activePathMotion, setActivePathMotion] = useState<ActivePathMotion | null>(null)
  const [pathRunner, setPathRunner] = useState<PathPoint | null>(null)
  const [completedTriggerIdsByFloor, setCompletedTriggerIdsByFloor] = useState<Record<number, string[]>>({})
  const [dwellTriggerKey, setDwellTriggerKey] = useState<string | null>(null)
  const [dwellSecondsLeft, setDwellSecondsLeft] = useState<number>(0)
  const [submittingKey, setSubmittingKey] = useState<string | null>(null)

  const dwellTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    let mounted = true
    const load = async () => {
      if (!buildingId && !poiId) {
        setError('缺少室內建築參數。')
        setLoading(false)
        return
      }
      setLoading(true)
      setError(null)
      try {
        const data = await api.public.getPublicIndoorBuilding({
          buildingId,
          poiId,
          locale,
        })
        if (!mounted) {
          return
        }
        setBuilding(data)
        const firstFloor = buildVirtualFloors(data)[0]
        setActiveFloorId(firstFloor?.id || null)
      } catch (loadError) {
        if (!mounted) {
          return
        }
        setError(loadError instanceof Error ? loadError.message : '室內資料載入失敗')
      } finally {
        if (mounted) {
          setLoading(false)
        }
      }
    }
    void load()
    return () => {
      mounted = false
    }
  }, [buildingId, poiId, locale])

  const baseFloors = useMemo(() => buildVirtualFloors(building), [building])

  const activeFloor = useMemo(() => {
    if (!activeFloorId) {
      return null
    }
    return floorCache[activeFloorId] || null
  }, [activeFloorId, floorCache])

  const activeBaseFloor = useMemo(
    () => baseFloors.find((floor) => floor.id === activeFloorId) || baseFloors[0] || null,
    [activeFloorId, baseFloors],
  )

  useEffect(() => {
    let mounted = true
    const loadFloor = async () => {
      if (!activeFloorId || activeFloorId <= 0) {
        return
      }
      if (floorCache[activeFloorId]) {
        return
      }
      setFloorLoadingId(activeFloorId)
      try {
        const detail = await fetchIndoorRuntimeFloorWithFallback({
          floorId: activeFloorId,
          locale,
        })
        if (!mounted) {
          return
        }
        setFloorCache((current) => ({
          ...current,
          [activeFloorId]: detail,
        }))
      } catch (floorError) {
        if (!mounted) {
          return
        }
        setRuntimeNotice(floorError instanceof Error ? floorError.message : '樓層 runtime 載入失敗')
      } finally {
        if (mounted) {
          setFloorLoadingId((current) => (current === activeFloorId ? null : current))
        }
      }
    }
    void loadFloor()
    return () => {
      mounted = false
    }
  }, [activeFloorId, floorCache, locale])

  useEffect(() => {
    setSelectedNodeId(null)
    setRuntimeNotice(null)
    setActivePopup(null)
    setActiveBubble(null)
    setActivePathMotion(null)
    setPathRunner(null)
    setDwellTriggerKey(null)
    setDwellSecondsLeft(0)
  }, [activeFloorId])

  useEffect(() => {
    if (!activeFloor?.nodes?.length) {
      return
    }
    setSelectedNodeId((current) => {
      if (current && activeFloor.nodes.some((node) => node.nodeId === current)) {
        return current
      }
      return activeFloor.nodes.find((node) => node.visible)?.nodeId || activeFloor.nodes[0]?.nodeId || null
    })
    const nextScale = clamp(activeFloor.defaultZoom || 1, activeFloor.zoomMin || 0.5, activeFloor.zoomMax || 3)
    setScale(nextScale)
  }, [activeFloor])

  useEffect(() => {
    if (!activePathMotion || activePathMotion.points.length < 2) {
      setPathRunner(activePathMotion?.points[0] || null)
      return
    }
    const points = activePathMotion.points
    const totalSegments = Math.max(points.length - 1, 1)
    const durationMs = Math.max(activePathMotion.durationMs || 2800, 1200)
    const holdMs = Math.max(activePathMotion.holdMs || 0, 0)
    const startedAt = Date.now()

    const timer = setInterval(() => {
      const elapsed = Date.now() - startedAt
      const progress = activePathMotion.loop
        ? (elapsed % durationMs) / durationMs
        : clamp(elapsed / durationMs, 0, 1)
      const segmentFloat = progress * totalSegments
      const segmentIndex = Math.min(totalSegments - 1, Math.floor(segmentFloat))
      const localProgress = clamp(segmentFloat - segmentIndex, 0, 1)
      const from = points[segmentIndex]
      const to = points[Math.min(segmentIndex + 1, points.length - 1)]
      setPathRunner({
        x: from.x + (to.x - from.x) * localProgress,
        y: from.y + (to.y - from.y) * localProgress,
      })

      if (!activePathMotion.loop && elapsed >= durationMs + holdMs) {
        clearInterval(timer)
      }
    }, 80)

    return () => {
      clearInterval(timer)
    }
  }, [activePathMotion])

  useEffect(() => {
    return () => {
      if (dwellTimeoutRef.current) {
        clearTimeout(dwellTimeoutRef.current)
      }
    }
  }, [])

  const parsedManifest = useMemo(
    () => parseTileManifest(activeFloor?.tileManifestJson),
    [activeFloor?.tileManifestJson],
  )

  const visibleTiles = useMemo(() => {
    if (!parsedManifest) {
      return []
    }
    return parsedManifest.tiles.filter((tile) => tile.z === parsedManifest.defaultLevel)
  }, [parsedManifest])

  const visibleNodes = useMemo(
    () => (activeFloor?.nodes || []).filter((node) => node.visible),
    [activeFloor?.nodes],
  )

  const selectedNode = useMemo(
    () => findIndoorRuntimeNode(activeFloor, selectedNodeId) || visibleNodes[0] || null,
    [activeFloor, selectedNodeId, visibleNodes],
  )

  const completedTriggerIds = useMemo(
    () => (activeFloorId ? completedTriggerIdsByFloor[activeFloorId] || [] : []),
    [activeFloorId, completedTriggerIdsByFloor],
  )

  const runnableTriggers = useMemo(
    () => getRunnableTriggerOptions(selectedNode, completedTriggerIds),
    [selectedNode, completedTriggerIds],
  )

  const previewImageUrl = activeFloor?.floorPlanUrl || activeFloor?.tilePreviewImageUrl
  const activeFloorLoading = !!activeFloorId && floorLoadingId === activeFloorId

  const overlayCards = useMemo(
    () => visibleNodes
      .map((node) => ({
        node,
        bounds: overlayBounds(node.overlayGeometry),
      }))
      .filter((item): item is { node: IndoorRuntimeNodeView; bounds: OverlayBounds } => !!item.bounds),
    [visibleNodes],
  )

  const pathSegments = useMemo(() => {
    const points = activePathMotion?.points || []
    if (points.length < 2) {
      return []
    }
    return points.slice(0, -1).map((point, index) => ({
      key: `${activePathMotion?.behaviorId || 'path'}-${index}`,
      from: point,
      to: points[index + 1],
    }))
  }, [activePathMotion])

  const floors = useMemo(
    () => baseFloors.map((floor) => ({
      ...floor,
      ...(floorCache[floor.id] || {}),
    })),
    [baseFloors, floorCache],
  )

  const finishTrigger = (matchedTriggerId?: string | null) => {
    if (!activeFloorId || !matchedTriggerId) {
      return
    }
    setCompletedTriggerIdsByFloor((current) => ({
      ...current,
      [activeFloorId]: Array.from(new Set([...(current[activeFloorId] || []), matchedTriggerId])),
    }))
  }

  const applyTriggeredEffects = (
    node: IndoorRuntimeNodeView,
    trigger: IndoorRuntimeTriggerOption,
    effects: PublicIndoorRuntimeTriggeredEffectDto[] = [],
  ) => {
    const popup = effects.find((effect) => effect.category === 'popup')
    if (popup) {
      setActivePopup(effectMessage(popup, trigger.label || markerLabel(node)))
    }

    const bubble = effects.find((effect) => effect.category === 'bubble')
    if (bubble) {
      setActiveBubble({
        ...effectMessage(bubble, trigger.label || markerLabel(node)),
        nodeId: node.nodeId,
      })
    } else {
      setActiveBubble(null)
    }

    const media = effects.find((effect) => effect.category === 'media')
    if (media && !popup) {
      setActivePopup(effectMessage(media, trigger.label || markerLabel(node)))
    }

    const motion = effects.find((effect) => effect.category === 'path_motion')
    if (motion) {
      const points = pathPoints(motion)
      if (points.length >= 2) {
        setActivePathMotion({
          nodeId: node.nodeId,
          behaviorId: trigger.behaviorId,
          label: motion.label || trigger.label,
          points,
          durationMs: motion.pathGraph?.durationMs || 3600,
          holdMs: motion.pathGraph?.holdMs || 300,
          loop: !!motion.pathGraph?.loop,
        })
      }
    }

    if (!effects.length) {
      setRuntimeNotice(`${trigger.label || markerLabel(node)} 已啟動。`)
      return
    }

    const renderedCategories = effects.map((effect) => effect.category || 'effect').join('、')
    setRuntimeNotice(`${trigger.label || markerLabel(node)} 已啟動：${renderedCategories}`)
  }

  const runInteraction = async (
    node: IndoorRuntimeNodeView,
    trigger: IndoorRuntimeTriggerOption,
    extra?: { dwellMs?: number },
  ) => {
    if (!activeFloorId) {
      return
    }

    if (trigger.requiresAuth) {
      const allowed = await requireAuth('啟動這個室內互動前，請先使用微信登入。')
      if (!allowed) {
        return
      }
    }

    const submissionKey = `${activeFloorId}:${trigger.behaviorId}:${trigger.triggerId || trigger.category}`
    setSubmittingKey(submissionKey)

    try {
      const response = await submitIndoorRuntimeInteraction({
        floorId: activeFloorId,
        nodeId: node.nodeId,
        behaviorId: trigger.behaviorId,
        triggerId: trigger.triggerId,
        eventType: trigger.category,
        relativeX: node.relativeX,
        relativeY: node.relativeY,
        dwellMs: extra?.dwellMs,
        locale,
        clientSessionId: getIndoorRuntimeSessionId(activeFloorId),
      })

      if (response.requiresAuth && !trigger.requiresAuth) {
        await requireAuth('這個室內互動需要先使用微信登入。')
      }

      finishTrigger(response.matchedTriggerId)

      if (response.interactionAccepted) {
        applyTriggeredEffects(node, trigger, response.effects || [])
      } else {
        setRuntimeNotice(describeIndoorBlockedReason(response.blockedReason))
      }
    } catch (interactionError) {
      console.warn('Failed to submit indoor runtime interaction.', interactionError)
      const message = interactionError instanceof Error ? interactionError.message : '室內互動提交失敗'
      setRuntimeNotice(message)
      Taro.showToast({
        title: '互動提交失敗',
        icon: 'none',
      })
    } finally {
      setSubmittingKey((current) => (current === submissionKey ? null : current))
    }
  }

  const startDwellInteraction = (node: IndoorRuntimeNodeView, trigger: IndoorRuntimeTriggerOption) => {
    if (dwellTimeoutRef.current) {
      clearTimeout(dwellTimeoutRef.current)
      dwellTimeoutRef.current = null
    }
    const seconds = trigger.dwellSeconds || 1
    const triggerKey = `${node.nodeId}:${trigger.behaviorId}:${trigger.triggerId || 'dwell'}`
    setDwellTriggerKey(triggerKey)
    setDwellSecondsLeft(seconds)
    setRuntimeNotice(`已開始停留互動，請等待 ${seconds} 秒。`)

    const tick = (nextSeconds: number) => {
      if (nextSeconds <= 0) {
        setDwellTriggerKey(null)
        setDwellSecondsLeft(0)
        void runInteraction(node, trigger, { dwellMs: seconds * 1000 })
        return
      }
      dwellTimeoutRef.current = setTimeout(() => {
        setDwellSecondsLeft(nextSeconds - 1)
        tick(nextSeconds - 1)
      }, 1000)
    }

    tick(seconds)
  }

  const handleTrigger = async (node: IndoorRuntimeNodeView, trigger: IndoorRuntimeTriggerOption) => {
    setSelectedNodeId(node.nodeId)
    if (trigger.category === 'dwell') {
      startDwellInteraction(node, trigger)
      return
    }
    await runInteraction(node, trigger)
  }

  const handleMarkerSelect = async (node: IndoorRuntimeNodeView) => {
    setSelectedNodeId(node.nodeId)
    const defaultTrigger = getDefaultTriggerOption(node, completedTriggerIds)
    if (defaultTrigger?.category === 'tap') {
      await handleTrigger(node, defaultTrigger)
    }
  }

  const zoomIn = () => setScale((value) => clamp(value + 0.2, activeFloor?.zoomMin || 0.5, activeFloor?.zoomMax || 3))
  const zoomOut = () => setScale((value) => clamp(value - 0.2, activeFloor?.zoomMin || 0.5, activeFloor?.zoomMax || 3))

  return (
    <PageShell className='indoor-page'>
      <View className='indoor-header'>
        <View className='indoor-header__title-box'>
          <Text className='indoor-header__title'>{building?.name || fallbackTitle}</Text>
          <Text className='indoor-header__subtitle'>
            {building?.address || (building?.bindingMode === 'poi' ? '綁定 POI 的室內場景' : '室內建築')}
          </Text>
        </View>
        <ScrollView className='floor-selector' scrollX>
          <View className='floor-selector__inner'>
            {floors.map((floor) => (
              <View
                key={floor.id}
                className={`floor-btn ${activeFloorId === floor.id ? 'active' : ''}`}
                onClick={() => setActiveFloorId(floor.id)}
              >
                <Text className='floor-btn__text'>{floorDisplayName(floor)}</Text>
              </View>
            ))}
          </View>
        </ScrollView>
      </View>

      <View className='indoor-map-container'>
        {loading ? (
          <View className='indoor-map-hint'>
            <Text className='indoor-map-hint__title'>正在載入室內資料</Text>
            <Text className='indoor-map-hint__desc'>系統會先讀取建築、樓層與已發佈的室內 runtime 設定。</Text>
          </View>
        ) : error ? (
          <View className='indoor-map-error'>
            <Text className='indoor-map-error__title'>室內資料載入失敗</Text>
            <Text className='indoor-map-error__desc'>{error}</Text>
          </View>
        ) : activeFloor ? (
          <View className='indoor-map-viewport'>
            <View
              className='indoor-map-content'
              style={{
                width: '100%',
                height: parsedManifest ? `${Math.max(70, (parsedManifest.gridRows / parsedManifest.gridCols) * 100)}%` : '100%',
                transform: `scale(${scale})`,
              }}
            >
              {visibleTiles.length ? (
                visibleTiles.map((tile) => (
                  <Image
                    key={`${tile.z}-${tile.x}-${tile.y}`}
                    className='indoor-map-tile'
                    src={tile.url}
                    mode='scaleToFill'
                    style={{
                      left: `${(tile.x / parsedManifest!.gridCols) * 100}%`,
                      top: `${(tile.y / parsedManifest!.gridRows) * 100}%`,
                      width: `${100 / parsedManifest!.gridCols}%`,
                      height: `${100 / parsedManifest!.gridRows}%`,
                    }}
                    showMenuByLongpress={false}
                  />
                ))
              ) : previewImageUrl ? (
                <Image
                  className='indoor-map-preview'
                  src={previewImageUrl}
                  mode='widthFix'
                  style={{ width: '100%' }}
                  showMenuByLongpress={false}
                />
              ) : (
                <View className='indoor-map-hint indoor-map-hint--embedded'>
                  <Text className='indoor-map-hint__title'>這一層尚未上傳圖資</Text>
                  <Text className='indoor-map-hint__desc'>
                    請回到後台為「{floorDisplayName(activeBaseFloor)}」上傳樓層地圖或瓦片資料。
                  </Text>
                </View>
              )}

              {overlayCards.map(({ node, bounds }) => (
                <View
                  key={`overlay-${node.nodeId}`}
                  className={`indoor-overlay indoor-overlay--${bounds.geometryType || 'shape'} ${selectedNode?.nodeId === node.nodeId ? 'is-selected' : ''}`}
                  style={{
                    left: `${bounds.left * 100}%`,
                    top: `${bounds.top * 100}%`,
                    width: `${bounds.width * 100}%`,
                    height: `${bounds.height * 100}%`,
                  }}
                  onClick={() => void handleMarkerSelect(node)}
                >
                  <Text className='indoor-overlay__label'>{bounds.label || markerLabel(node)}</Text>
                </View>
              ))}

              {pathSegments.map((segment) => (
                <View
                  key={segment.key}
                  className='indoor-path-segment'
                  style={motionSegmentStyle(segment.from, segment.to)}
                />
              ))}

              {pathRunner ? (
                <View
                  className='indoor-path-runner'
                  style={{
                    left: `${pathRunner.x * 100}%`,
                    top: `${pathRunner.y * 100}%`,
                  }}
                />
              ) : null}

              {visibleNodes.map((node) => (
                <View
                  key={node.nodeId}
                  className={`indoor-marker ${selectedNode?.nodeId === node.nodeId ? 'is-selected' : ''} ${node.blockedBehaviorCount ? 'is-blocked' : ''}`}
                  style={{
                    left: `${(node.relativeX || 0) * 100}%`,
                    top: `${(node.relativeY || 0) * 100}%`,
                  }}
                  onClick={() => void handleMarkerSelect(node)}
                >
                  {node.iconUrl ? (
                    <Image className='indoor-marker__icon' src={node.iconUrl} mode='aspectFit' showMenuByLongpress={false} />
                  ) : (
                    <View className='indoor-marker__dot' />
                  )}
                  <Text className='indoor-marker__label'>{markerLabel(node)}</Text>
                </View>
              ))}

              {activeBubble && selectedNode ? (
                <View
                  className='indoor-effect-bubble'
                  style={{
                    left: `${(selectedNode.relativeX || 0) * 100}%`,
                    top: `${((selectedNode.relativeY || 0) * 100) - 10}%`,
                  }}
                >
                  <Text className='indoor-effect-bubble__title'>{activeBubble.title}</Text>
                  <Text className='indoor-effect-bubble__body'>{activeBubble.body}</Text>
                </View>
              ) : null}
            </View>

            {activeFloorLoading ? (
              <View className='indoor-loading-mask'>
                <View className='indoor-loading-mask__card'>
                  <Text className='indoor-loading-mask__title'>樓層 runtime 載入中</Text>
                  <Text className='indoor-loading-mask__desc'>正在同步圖資、標記與互動規則，完成後才會顯示。</Text>
                </View>
              </View>
            ) : null}
          </View>
        ) : (
          <View className='indoor-map-hint'>
            <Text className='indoor-map-hint__title'>尚未建立樓層</Text>
            <Text className='indoor-map-hint__desc'>請先在後台建立至少一個樓層，再回到小程序查看。</Text>
          </View>
        )}

        <View className='map-tools'>
          <View className='map-tools__group'>
            <Button className='map-tool-btn' onClick={zoomIn}>放大</Button>
            <Button className='map-tool-btn' onClick={zoomOut}>縮小</Button>
          </View>
        </View>

        {runtimeNotice ? (
          <View className='indoor-runtime-toast'>
            <Text className='indoor-runtime-toast__text'>{runtimeNotice}</Text>
          </View>
        ) : null}

        {activeFloor ? (
          <View className='indoor-runtime-drawer'>
            <View className='indoor-runtime-drawer__header'>
              <View>
                <Text className='indoor-runtime-drawer__title'>
                  {selectedNode ? markerLabel(selectedNode) : floorDisplayName(activeFloor)}
                </Text>
                <Text className='indoor-runtime-drawer__subtitle'>
                  {activeFloor.source === 'runtime'
                    ? `Runtime 版本 ${activeFloor.runtimeVersion || '載入中'}`
                    : '目前為靜態 fallback，互動規則尚未連上。'}
                </Text>
              </View>
              {selectedNode?.blockedBehaviorCount ? (
                <Text className='indoor-runtime-chip indoor-runtime-chip--warn'>
                  有 {selectedNode.blockedBehaviorCount} 條規則暫不可執行
                </Text>
              ) : null}
            </View>

            {selectedNode?.description ? (
              <Text className='indoor-runtime-drawer__body'>{selectedNode.description}</Text>
            ) : (
              <Text className='indoor-runtime-drawer__body'>選取一個標記點，查看它的互動規則與即時效果。</Text>
            )}

            {selectedNode ? (
              <ScrollView className='indoor-runtime-trigger-list' scrollX>
                <View className='indoor-runtime-trigger-list__inner'>
                  {runnableTriggers.length ? runnableTriggers.map((trigger) => {
                    const key = `${activeFloor.floorId}:${trigger.behaviorId}:${trigger.triggerId || trigger.category}`
                    const waitingDwell = dwellTriggerKey === key
                    return (
                      <Button
                        key={key}
                        className={`indoor-runtime-trigger ${trigger.supported ? 'is-supported' : 'is-blocked'}`}
                        onClick={() => void handleTrigger(selectedNode, trigger)}
                        disabled={submittingKey === key}
                      >
                        {waitingDwell ? `等待中 ${dwellSecondsLeft}s` : triggerButtonLabel(trigger)}
                      </Button>
                    )
                  }) : (
                    <View className='indoor-runtime-empty'>
                      <Text className='indoor-runtime-empty__text'>這個節點目前沒有可執行的互動。</Text>
                    </View>
                  )}
                </View>
              </ScrollView>
            ) : null}

            {selectedNode?.behaviors?.length ? (
              <View className='indoor-runtime-behavior-list'>
                {selectedNode.behaviors.map((behavior) => (
                  <View key={behavior.behaviorId} className='indoor-runtime-behavior-card'>
                    <View className='indoor-runtime-behavior-card__header'>
                      <Text className='indoor-runtime-behavior-card__title'>{behavior.name || behavior.behaviorCode}</Text>
                      <Text className={`indoor-runtime-chip ${behavior.supported ? 'indoor-runtime-chip--ok' : 'indoor-runtime-chip--warn'}`}>
                        {behavior.supported ? '可執行' : '已阻擋'}
                      </Text>
                    </View>
                    <Text className='indoor-runtime-behavior-card__meta'>
                      觸發：{(behavior.triggerRules || []).map((rule) => rule.category).filter(Boolean).join('、') || '無'}
                    </Text>
                    <Text className='indoor-runtime-behavior-card__meta'>
                      效果：{(behavior.effectRules || []).map((rule) => rule.category).filter(Boolean).join('、') || '無'}
                    </Text>
                    {!behavior.supported && behavior.blockedReason ? (
                      <Text className='indoor-runtime-behavior-card__warn'>
                        {describeIndoorBlockedReason(behavior.blockedReason)}
                      </Text>
                    ) : null}
                  </View>
                ))}
              </View>
            ) : null}
          </View>
        ) : null}
      </View>

      {building?.description ? (
        <View className='indoor-info-card'>
          <Text className='indoor-info-card__title'>建築介紹</Text>
          <Text className='indoor-info-card__body'>{building.description}</Text>
        </View>
      ) : null}

      {activePopup ? (
        <View className='indoor-popup'>
          <View className='indoor-popup__mask' onClick={() => setActivePopup(null)} />
          <View className='indoor-popup__card'>
            <Text className='indoor-popup__category'>{activePopup.category}</Text>
            <Text className='indoor-popup__title'>{activePopup.title}</Text>
            <Text className='indoor-popup__body'>{activePopup.body}</Text>
            <Button className='indoor-popup__button' onClick={() => setActivePopup(null)}>關閉</Button>
          </View>
        </View>
      ) : null}
    </PageShell>
  )
}
