import React from 'react';
import type { NamePath } from 'antd/es/form/interface';
import MediaAssetPickerField from '../media/MediaAssetPickerField';

interface SpatialAssetPickerFieldProps {
  name: NamePath;
  label: string;
  assetKind?: string;
  required?: boolean;
  allowClear?: boolean;
  placeholder?: string;
  help?: string;
  multiple?: boolean;
  onValueChange?: (value: number | string | Array<number | string> | null | undefined) => void;
  defaultCapabilityCode?: 'admin_image_generation' | 'admin_tts_generation' | 'admin_prompt_drafting';
  defaultGenerationType?: 'text' | 'image' | 'tts';
  defaultPromptTitle?: string;
  defaultPromptText?: string;
  defaultSourceScope?: string;
  defaultSourceScopeId?: number;
}

const SpatialAssetPickerField: React.FC<SpatialAssetPickerFieldProps> = (props) => (
  <MediaAssetPickerField {...props} valueMode="asset-id" />
);

export default SpatialAssetPickerField;
