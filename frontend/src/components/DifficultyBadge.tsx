interface Props {
  difficulty: 'easy' | 'medium' | 'hard';
  size?: 'sm' | 'md';
}

export function DifficultyBadge({ difficulty, size = 'sm' }: Props) {
  const baseClasses = 'rounded-full font-medium capitalize';
  
  const sizeClasses = size === 'sm' 
    ? 'px-2.5 py-0.5 text-xs' 
    : 'px-3 py-1 text-sm';
  
  const colorClasses = {
    easy: 'difficulty-easy',
    medium: 'difficulty-medium',
    hard: 'difficulty-hard'
  };

  return (
    <span className={`${baseClasses} ${sizeClasses} ${colorClasses[difficulty]}`}>
      {difficulty}
    </span>
  );
}

