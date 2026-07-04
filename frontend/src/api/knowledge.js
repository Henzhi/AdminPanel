import request from './request'

export function getKnowledgeList(params) {
  return request.get('/knowledge', { params })
}

export function getKnowledgeDetail(id) {
  return request.get(`/knowledge/${id}`)
}

export function createKnowledge(data) {
  return request.post('/knowledge', data)
}

export function updateKnowledge(id, data) {
  return request.put(`/knowledge/${id}`, data)
}

export function deleteKnowledge(id) {
  return request.delete(`/knowledge/${id}`)
}

export function syncKnowledge(id) {
  return request.post(`/knowledge/${id}/sync`)
}
