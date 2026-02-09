import { useRef, useEffect } from 'react';
import type { Language } from '../types';
import { LANGUAGES } from '../types';

interface Props {
  code: string;
  onChange: (code: string) => void;
  language: Language;
  onLanguageChange: (lang: Language) => void;
  disabled?: boolean;
}

export function CodeEditor({ code, onChange, language, onLanguageChange, disabled }: Props) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Handle tab key
  useEffect(() => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Tab') {
        e.preventDefault();
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const newValue = code.substring(0, start) + '    ' + code.substring(end);
        onChange(newValue);
        // Set cursor position after tab
        setTimeout(() => {
          textarea.selectionStart = textarea.selectionEnd = start + 4;
        }, 0);
      }
    };

    textarea.addEventListener('keydown', handleKeyDown);
    return () => textarea.removeEventListener('keydown', handleKeyDown);
  }, [code, onChange]);

  return (
    <div className="flex flex-col h-full">
      {/* Toolbar */}
      <div className="flex items-center justify-between px-4 py-2 bg-dark-700 border-b border-gray-700">
        <select
          value={language}
          onChange={(e) => onLanguageChange(e.target.value as Language)}
          disabled={disabled}
          className="bg-dark-600 border border-gray-600 rounded-lg px-3 py-1.5 text-sm text-gray-200 focus:outline-none focus:border-indigo-500 disabled:opacity-50"
        >
          {Object.entries(LANGUAGES).map(([key, value]) => (
            <option key={key} value={key}>{value.name}</option>
          ))}
        </select>
        
        <button
          onClick={() => onChange(LANGUAGES[language].starterCode)}
          disabled={disabled}
          className="text-xs text-gray-400 hover:text-white transition-colors disabled:opacity-50"
        >
          Reset Code
        </button>
      </div>

      {/* Editor */}
      <div className="flex-1 relative">
        <textarea
          ref={textareaRef}
          value={code}
          onChange={(e) => onChange(e.target.value)}
          disabled={disabled}
          spellCheck={false}
          className="code-editor w-full h-full bg-dark-800 text-gray-100 p-4 resize-none focus:outline-none disabled:opacity-50"
          placeholder="Write your code here..."
        />
      </div>
    </div>
  );
}

