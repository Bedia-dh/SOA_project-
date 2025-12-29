import React, { useState, useEffect } from 'react';
import './App.css';
import { personService } from './services/personService';
import Header from './components/Header';
import SearchBar from './components/SearchBar';
import PersonTable from './components/PersonTable';
import PersonForm from './components/PersonForm';

function App() {
  const [persons, setPersons] = useState([]);
  const [status, setStatus] = useState({ message: 'Ready', isError: false });
  const [editingPerson, setEditingPerson] = useState(null);

  useEffect(() => {
    loadPersons();
  }, []);

  const updateStatus = (message, isError = false) => {
    setStatus({ message, isError });
  };

  const loadPersons = async () => {
    try {
      updateStatus('Loading…');
      const data = await personService.getAll();
      setPersons(data);
      updateStatus('Loaded');
    } catch (err) {
      updateStatus(err.message, true);
    }
  };

  const handleSearch = async (query) => {
    if (!query.trim()) {
      updateStatus('Please enter a name or ID', true);
      return;
    }

    try {
      updateStatus('Searching…');
      if (!isNaN(query)) {
        // Search by ID
        const person = await personService.getById(Number(query));
        if (person) {
          setPersons([person]);
          updateStatus(`Found by ID: ${query}`);
        } else {
          setPersons([]);
          updateStatus(`No person found with ID: ${query}`);
        }
      } else {
        // Search by name
        const results = await personService.searchByName(query);
        setPersons(results);
        if (results.length === 0) {
          updateStatus(`No results found for: "${query}"`);
        } else {
          updateStatus(`Found ${results.length} result(s) for: "${query}"`);
        }
      }
    } catch (err) {
      updateStatus(err.message, true);
      setPersons([]);
    }
  };

  const handleClearSearch = () => {
    loadPersons();
  };

  const handleAdd = async (person) => {
    try {
      await personService.create(person);
      updateStatus('Added person');
      loadPersons();
      return true;
    } catch (err) {
      updateStatus(err.message, true);
      return false;
    }
  };

  const handleUpdate = async (id, person) => {
    try {
      await personService.update(id, person);
      updateStatus(`Updated #${id}`);
      loadPersons();
      setEditingPerson(null);
      return true;
    } catch (err) {
      updateStatus(err.message, true);
      return false;
    }
  };

  const handleEdit = (person) => {
    setEditingPerson(person);
  };

  const handleCancelEdit = () => {
    setEditingPerson(null);
  };

  const handleDelete = async (id) => {
    if (!window.confirm(`Delete person #${id}? This cannot be undone.`)) {
      updateStatus('Deletion cancelled');
      return;
    }

    try {
      await personService.delete(id);
      updateStatus(`Deleted #${id}`);
      if (editingPerson && editingPerson.id === id) {
        setEditingPerson(null);
      }
      loadPersons();
    } catch (err) {
      updateStatus(err.message, true);
    }
  };

  return (
    <div className="page">
      <Header status={status} />
      
      <SearchBar 
        onSearch={handleSearch}
        onClear={handleClearSearch}
      />

      <PersonTable
        persons={persons}
        onEdit={handleEdit}
        onDelete={handleDelete}
        onRefresh={loadPersons}
      />

      <PersonForm
        editingPerson={editingPerson}
        onAdd={handleAdd}
        onUpdate={handleUpdate}
        onCancel={handleCancelEdit}
      />
    </div>
  );
}

export default App;
