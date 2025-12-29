const API_BASE = 'api/persons';

export const personService = {
  async getAll() {
    const res = await fetch(API_BASE);
    if (!res.ok) throw new Error(`Failed to fetch: ${res.status}`);
    return res.json();
  },

  async getById(id) {
    const res = await fetch(`${API_BASE}/${id}`);
    if (!res.ok) {
      if (res.status === 404) return null;
      throw new Error(`Failed to fetch: ${res.status}`);
    }
    return res.json();
  },

  async searchByName(name) {
    const res = await fetch(`${API_BASE}/search?name=${encodeURIComponent(name)}`);
    if (!res.ok) throw new Error(`Failed to search: ${res.status}`);
    return res.json();
  },

  async create(person) {
    const res = await fetch(API_BASE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(person)
    });
    if (!res.ok) throw new Error(`Failed to create: ${res.status}`);
    return res.json();
  },

  async update(id, person) {
    const res = await fetch(`${API_BASE}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(person)
    });
    if (!res.ok) throw new Error(`Failed to update: ${res.status}`);
    return res.json();
  },

  async delete(id) {
    const res = await fetch(`${API_BASE}/${id}`, {
      method: 'DELETE'
    });
    if (!res.ok) throw new Error(`Failed to delete: ${res.status}`);
  }
};
