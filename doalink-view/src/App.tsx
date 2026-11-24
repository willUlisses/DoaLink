import { BrowserRouter } from 'react-router-dom'
import { ToastContainer } from './components/feedback/ToastContainer.tsx'
import { AuthProvider } from './contexts/AuthContext.tsx'
import { ToastProvider } from './contexts/ToastContext.tsx'
import { AppRoutes } from './routes/router.tsx'

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
        <ToastContainer />
      </AuthProvider>
    </ToastProvider>
  )
}

export default App
