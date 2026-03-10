// src/constants/fileTypes.ts

export type AllowedFileType = {
  mime: string
  preview: 'video' | 'image' | 'none'
  label: string
}

export const allowedFileTypes: AllowedFileType[] = [
  { mime: 'video/mp4', preview: 'video', label: 'MP4' },
  { mime: 'video/x-msvideo', preview: 'video', label: 'AVI' },
  { mime: 'video/x-matroska', preview: 'video', label: 'MKV' },
  { mime: 'video/quicktime', preview: 'video', label: 'MOV' },
  { mime: 'video/x-ms-wmv', preview: 'video', label: 'WMV' },
  { mime: 'video/mp2t', preview: 'video', label: 'TS' },
  { mime: 'image/jpg', preview: 'image', label: 'JPG' },
  { mime: 'image/jpeg', preview: 'image', label: 'JPEG' },
  { mime: 'application/pdf', preview: 'image', label: 'PDF' },
]

export const allowedMimeTypes = allowedFileTypes.map((t) => t.mime)
