import axios from 'axios'

const service = axios.create({
  baseURL: '/',
  withCredentials: true,
  timeout: 30000
})

const TOKEN_KEY = 'auth_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准封装（如流式响应）直接返回
    if (res === null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code !== 200) {
      if (res.code === 401) {
        setToken(null)
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      setToken(null)
      window.dispatchEvent(new CustomEvent('auth:expired'))
    }
    const msg =
      error.response?.data?.message ||
      (status ? `请求失败 (${status})` : '网络异常，请检查后端服务')
    return Promise.reject(new Error(msg))
  }
)

/** 统一请求入口：GET */
export function get(url, params, config) {
  return service.get(url, { params, ...config })
}

export function post(url, data, config) {
  return service.post(url, data, config)
}

export function put(url, data, config) {
  return service.put(url, data, config)
}

export function del(url, params, config) {
  return service.delete(url, { params, ...config })
}

export default service