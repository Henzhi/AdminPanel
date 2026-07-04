import request from './request'

export function getArtifactList(params) {
  return request.get('/artifacts', { params })
}

export function getArtifactDetail(id) {
  return request.get(`/artifacts/${id}`)
}

export function createArtifact(data) {
  return request.post('/artifacts', data)
}

export function updateArtifact(id, data) {
  return request.put(`/artifacts/${id}`, data)
}

export function deleteArtifact(id) {
  return request.delete(`/artifacts/${id}`)
}

export function importArtifacts(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/artifacts/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function exportArtifacts(params) {
  return request.get('/artifacts/export', {
    params,
    responseType: 'blob',
  })
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/artifacts/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function getCategories() {
  return request.get('/artifacts/categories')
}
