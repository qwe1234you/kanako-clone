import { get, post, put, del } from './client'

export const blogApi = {
  page: (params) => get('/api-common/blog/page', params),
  detail: (id) => get(`/api-common/blog/${id}`),
  detailBySlug: (slug) => get(`/api-common/blog/${encodeURIComponent(slug)}`),
  categories: async () => {
    const list = await get('/api-common/blog/categories')
    return (list || []).map((i) => ({ ...(i.tag || {}), count: i.count }))
  },
  tags: async () => {
    const list = await get('/api-common/blog/tags')
    return (list || []).map((i) => ({ ...(i.tag || {}), count: i.count }))
  },
  archive: () => get('/api-common/blog/archive'),
  create: (data) => post('/api-common/blog', data),
  update: (id, data) => put(`/api-common/blog/${id}`, data),
  remove: (id) => del(`/api-common/blog/${id}`),
  like: (id) => post(`/api-common/blog/${id}/like`),
  search: (params) => get('/api-common/search', params),
  comments: (id) => get(`/api-common/blog/${id}/comments`),
  addComment: (id, params) => post(`/api-common/blog/${id}/comments`, null, { params }),
  adminPage: (params) => get('/api-common/blog/admin/page', params),
  adminDetail: (id) => get(`/api-common/blog/admin/${id}`),
  saveCategory: (data) => post('/api-common/blog/categories', null, { params: data }),
  deleteCategory: (id) => del(`/api-common/blog/categories/${id}`),
  saveTag: (data) => post('/api-common/blog/tags', null, { params: data }),
  deleteTag: (id) => del(`/api-common/blog/tags/${id}`)
}

export const siteApi = {
  config: () => get('/api-common/site/config'),
  updateConfig: (data) => put('/api-common/site/config', data),
  stats: () => get('/api-common/analytics/summary'),
  changelog: () => get('/api-common/update/list')
}

export const authApi = {
  login: (data) => post('/api-common/auth/login', data),
  me: () => get('/api-common/auth/me'),
  githubStart: (redirectUri) =>
    get('/api-common/auth/oauth/github/start', { redirectUri }),
  githubCallback: (params) =>
    get(`/api-common/auth/oauth/github/callback?${new URLSearchParams(params).toString()}`)
}

export const treeholeApi = {
  page: (params) => get('/api-common/treehole/page', params),
  detail: (id) => get(`/api-common/treehole/${id}`),
  create: (data) => post('/api-common/treehole', data),
  like: (id) => post(`/api-common/treehole/${id}/like`),
  comments: (id) => get(`/api-common/treehole/${id}/comments`),
  comment: (id, data) => post(`/api-common/treehole/${id}/comments`, data)
}

export const friendApi = {
  links: () => get('/api-common/friend/links'),
  circle: (params) => get('/api-common/friend/circle', params),
  apply: (data) => post('/api-common/friend/apply', data),
  adminLinks: (params) => get('/api-common/friend/admin/links', params),
  setLinkStatus: (id, status) => post(`/api-common/friend/admin/link/${id}`, null, { params: { status } }),
  deleteLink: (id) => del(`/api-common/friend/admin/link/${id}`),
  createCircle: (data) => post('/api-common/friend/admin/circle', data),
  deleteCircle: (id) => del(`/api-common/friend/admin/circle/${id}`)
}

export const chatApi = {
  chatNew: () => post('/api-llm/chat/new', {}),
  chatHistory: (sessionId) => get('/api-llm/chat/history', { sessionId }),
  chatSend: (sessionId, message) => post('/api-llm/chat/send', { sessionId, message })
}

export const ossApi = {  list: (params) => get('/api-common/oss/fetchDirFile', params),
  upload: (files, path) => {
    const form = new FormData()
    for (const f of files) form.append('files', f)
    return post('/api-common/oss/uploadFiles', form, { params: { path } })
  },
  mkdir: (path, dirName) => post('/api-common/oss/makeDirectory', null, { params: { path, dirName } }),
  rename: (path, newName) => post('/api-common/oss/renameFile', null, { params: { path, newName } }),
  remove: (path) => post('/api-common/oss/deleteFile', null, { params: { path } })
}