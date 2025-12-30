import request from '@/utils/request'

/**
 * 文件上传API
 */

// 上传任务凭证图片
export function uploadTaskProof(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/upload/task-proof',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
