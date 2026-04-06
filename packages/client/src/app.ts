import { ComponentType } from 'react';
import { View, Text } from '@tarojs/components';
import './app.css';

// 小程序全局入口
// 由 Taro 框架加載

function App({ children }: { children: ComponentType }) {
  return (
    <View className="app-container">
      {children}
    </View>
  );
}

export default App;
