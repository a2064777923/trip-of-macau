import React from 'react';
import { GameRewardWorkspace } from './GameRewardManagement';

const HonorManagement: React.FC = () => (
  <GameRewardWorkspace
    honorsOnly
    title="榮譽與稱號"
    subTitle="以遊戲內獎勵的榮譽子視角管理徽章、稱號與相關獲得演出，不再維護另一套分離資料模型。"
  />
);

export default HonorManagement;
