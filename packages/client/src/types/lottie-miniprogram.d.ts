declare module 'lottie-miniprogram' {
  interface LottieAnimationInstance {
    play?: () => void
    stop?: () => void
    destroy?: () => void
    pause?: () => void
  }

  interface LottieLoadAnimationOptions {
    loop?: boolean
    autoplay?: boolean
    animationData?: Record<string, unknown>
    path?: string
    rendererSettings: {
      context: any
    }
  }

  interface LottieMiniProgram {
    setup: (canvas: any) => void
    loadAnimation: (options: LottieLoadAnimationOptions) => LottieAnimationInstance
  }

  const lottie: LottieMiniProgram
  export default lottie
}
