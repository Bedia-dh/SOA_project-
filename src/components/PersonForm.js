import React, { useState, useEffect } from 'react';

function PersonForm({ editingPerson, onAdd, onUpdate, onCancel }) {
  const [formData, setFormData] = useState({
    id: '',
    name: '',
    age: ''
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (editingPerson) {
      setFormData({
        id: editingPerson.id,
        name: editingPerson.name,
        age: editingPerson.age
      });
    }
  }, [editingPerson]);

  const validate = () => {
    const newErrors = {};
    
    if (editingPerson) {
      const id = Number(formData.id);
      if (!Number.isInteger(id) || id < 1) {
        newErrors.id = 'ID must be a positive integer';
      }
    }
    
    if (!formData.name || formData.name.trim().length < 2) {
      newErrors.name = 'Name must have at least 2 characters';
    }
    
    const age = Number(formData.age);
    if (!Number.isInteger(age) || age < 0) {
      newErrors.age = 'Age must be a non-negative integer';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validate()) {
      return;
    }

    const payload = {
      name: formData.name.trim(),
      age: Number(formData.age)
    };

    let success;
    if (editingPerson) {
      payload.id = editingPerson.id;
      success = await onUpdate(editingPerson.id, payload);
    } else {
      success = await onAdd(payload);
    }

    if (success) {
      handleReset();
    }
  };

  const handleReset = () => {
    setFormData({ id: '', name: '', age: '' });
    setErrors({});
    if (editingPerson) {
      onCancel();
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    // Clear error for this field
    if (errors[e.target.name]) {
      setErrors({
        ...errors,
        [e.target.name]: undefined
      });
    }
  };

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Add / Edit Person</h2>
          <p className="sub">Submit to create, or prefill by clicking edit.</p>
        </div>
        <button className="ghost" onClick={handleReset}>
          Clear form
        </button>
      </div>
      <form className="form" onSubmit={handleSubmit}>
        {editingPerson && (
          <div className="field">
            <label htmlFor="id">ID</label>
            <input
              type="number"
              id="id"
              name="id"
              value={formData.id}
              onChange={handleChange}
              required
              min="1"
              disabled
            />
            {errors.id && <span style={{ color: '#fecdd3', fontSize: '12px' }}>{errors.id}</span>}
          </div>
        )}
        <div className="field">
          <label htmlFor="name">Name</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
          {errors.name && <span style={{ color: '#fecdd3', fontSize: '12px' }}>{errors.name}</span>}
        </div>
        <div className="field">
          <label htmlFor="age">Age</label>
          <input
            type="number"
            id="age"
            name="age"
            value={formData.age}
            onChange={handleChange}
            required
            min="0"
          />
          {errors.age && <span style={{ color: '#fecdd3', fontSize: '12px' }}>{errors.age}</span>}
        </div>
        <div className="form-actions">
          <button type="submit" className="primary">
            {editingPerson ? 'Update' : 'Save'}
          </button>
        </div>
      </form>
    </section>
  );
}

export default PersonForm;
