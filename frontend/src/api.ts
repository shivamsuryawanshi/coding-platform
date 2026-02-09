import type { ProblemListItem, ProblemDetail, SubmissionRequest, SubmissionResponse, Stats } from './types';

const API_BASE = '/api';

/**
 * API client for CodeNexus backend.
 */
export const api = {
  /**
   * Get all problems with optional filters.
   */
  async getProblems(params?: {
    category?: string;
    difficulty?: string;
    search?: string;
  }): Promise<ProblemListItem[]> {
    const url = new URL(`${API_BASE}/problems`, window.location.origin);
    
    if (params?.category) url.searchParams.set('category', params.category);
    if (params?.difficulty) url.searchParams.set('difficulty', params.difficulty);
    if (params?.search) url.searchParams.set('search', params.search);
    
    const response = await fetch(url.toString());
    if (!response.ok) throw new Error('Failed to fetch problems');
    return response.json();
  },

  /**
   * Get problem details by ID.
   */
  async getProblem(id: string): Promise<ProblemDetail> {
    const response = await fetch(`${API_BASE}/problems/${id}`);
    if (!response.ok) {
      if (response.status === 404) throw new Error('Problem not found');
      throw new Error('Failed to fetch problem');
    }
    return response.json();
  },

  /**
   * Get all categories.
   */
  async getCategories(): Promise<string[]> {
    const response = await fetch(`${API_BASE}/categories`);
    if (!response.ok) throw new Error('Failed to fetch categories');
    return response.json();
  },

  /**
   * Get statistics.
   */
  async getStats(): Promise<Stats> {
    const response = await fetch(`${API_BASE}/stats`);
    if (!response.ok) throw new Error('Failed to fetch stats');
    return response.json();
  },

  /**
   * Submit code for evaluation.
   */
  async submit(request: SubmissionRequest): Promise<SubmissionResponse> {
    const response = await fetch(`${API_BASE}/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request)
    });
    
    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.error || 'Submission failed');
    }
    
    return response.json();
  },

  /**
   * Health check.
   */
  async health(): Promise<{ status: string; judge: boolean }> {
    const response = await fetch(`${API_BASE}/health`);
    return response.json();
  }
};

