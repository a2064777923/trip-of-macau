import { PropsWithChildren, useEffect, useState } from 'react'
import { View } from '@tarojs/components'
import { useDidShow } from '@tarojs/taro'
import { loadGameState } from '../services/gameService'

interface PageShellProps {
  className?: string
}

export default function PageShell({ className = '', children }: PropsWithChildren<PageShellProps>) {
  const [user, setUser] = useState(() => loadGameState().user)

  const refreshPreference = () => {
    setUser(loadGameState().user)
  }

  useEffect(() => {
    refreshPreference()
  }, [])

  useDidShow(() => {
    refreshPreference()
  })

  const classes = [
    'page-shell',
    `app-mode--${user.interfaceMode}`,
    user.highContrast ? 'app-mode--high-contrast' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <View className={classes} style={{ fontSize: `${28 * user.fontScale}px` }}>
      {children}
    </View>
  )
}
