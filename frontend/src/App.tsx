import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Header } from './components/Header';
import { ProblemListPage } from './pages/ProblemListPage';
import { ProblemDetailPage } from './pages/ProblemDetailPage';

function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-dark-900">
        <Header />
        <Routes>
          <Route path="/" element={<ProblemListPage />} />
          <Route path="/problem/:id" element={<ProblemDetailPage />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
