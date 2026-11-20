import { useCallback, useEffect, useReducer, useRef, useState } from 'react'
import { normalizeApiError } from '../services/api.ts'
import type { NormalizedApiError } from '../types/api.ts'

interface ResourceState<T> {
  data: T | null
  loading: boolean
  error: NormalizedApiError | null
}

type ResourceAction<T> =
  | { type: 'start' }
  | { type: 'success'; data: T }
  | { type: 'error'; error: NormalizedApiError }

function resourceReducer<T>(
  state: ResourceState<T>,
  action: ResourceAction<T>,
): ResourceState<T> {
  switch (action.type) {
    case 'start':
      return { ...state, loading: true, error: null }
    case 'success':
      return { data: action.data, loading: false, error: null }
    case 'error':
      return { ...state, loading: false, error: action.error }
  }
}

export function useAsyncResource<T>(
  fetcher: () => Promise<T>,
  deps: readonly unknown[],
  enabled = true,
) {
  const [state, dispatch] = useReducer(resourceReducer<T>, {
    data: null,
    loading: enabled,
    error: null,
  })
  const [reloadCount, setReloadCount] = useState(0)
  const fetcherRef = useRef(fetcher)

  useEffect(() => {
    fetcherRef.current = fetcher
  }, [fetcher])

  useEffect(() => {
    if (!enabled) return
    let cancelled = false

    Promise.resolve()
      .then(() => {
        if (cancelled) return undefined
        dispatch({ type: 'start' })
        return fetcherRef.current()
      })
      .then((result) => {
        if (!cancelled && result !== undefined) {
          dispatch({ type: 'success', data: result })
        }
      })
      .catch((err) => {
        if (!cancelled) {
          dispatch({ type: 'error', error: normalizeApiError(err) })
        }
      })

    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, reloadCount, ...deps])

  const reload = useCallback(() => {
    setReloadCount((c) => c + 1)
  }, [])

  return {
    data: state.data,
    loading: state.loading,
    error: state.error,
    reload,
  }
}

