import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export function Header() {
  const location = useLocation();
  const { isAuthenticated, user, logout } = useAuth();
  
  const isActive = (path: string) => location.pathname === path;
  
  return (
    <header className="bg-dark-800/80 backdrop-blur-md border-b border-gray-800 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-10 h-10 bg-gradient-to-br from-indigo-500 to-violet-600 rounded-xl flex items-center justify-center shadow-lg shadow-indigo-500/20 group-hover:shadow-indigo-500/40 transition-shadow">
              <span className="text-white font-bold text-lg">CN</span>
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-indigo-400 to-violet-400 bg-clip-text text-transparent">
              CodeNexus
            </span>
          </Link>

          {/* Navigation */}
          <nav className="flex items-center gap-6">
            <Link
              to="/"
              className={`text-sm font-medium transition-colors ${
                isActive('/') 
                  ? 'text-indigo-400' 
                  : 'text-gray-400 hover:text-white'
              }`}
            >
              Problems
            </Link>
            
            {isAuthenticated ? (
              <>
                <Link
                  to="/submissions"
                  className={`text-sm font-medium transition-colors ${
                    isActive('/submissions') 
                      ? 'text-indigo-400' 
                      : 'text-gray-400 hover:text-white'
                  }`}
                >
                  My Submissions
                </Link>
                <div className="flex items-center gap-4">
                  <span className="text-gray-400 text-sm">{user?.email}</span>
                  <button
                    onClick={logout}
                    className="px-4 py-2 bg-dark-700 hover:bg-dark-600 text-gray-300 rounded-lg text-sm transition-colors"
                  >
                    Logout
                  </button>
                </div>
              </>
            ) : (
              <Link
                to="/auth"
                className="px-4 py-2 bg-gradient-to-r from-indigo-500 to-violet-500 hover:from-indigo-600 hover:to-violet-600 text-white rounded-lg text-sm font-medium transition-all"
              >
                Login
              </Link>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
}

