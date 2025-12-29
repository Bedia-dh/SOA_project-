import React from 'react';

function PersonTable({ persons, onEdit, onDelete, onRefresh }) {
  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Directory</h2>
          <p className="sub">Live data from /api/persons</p>
        </div>
        <button className="ghost" onClick={onRefresh}>
          Refresh
        </button>
      </div>
      <div className="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Age</th>
              <th className="actions-col">Actions</th>
            </tr>
          </thead>
          <tbody>
            {persons.length === 0 ? (
              <tr>
                <td colSpan="4" className="muted">
                  No persons found. Add one below.
                </td>
              </tr>
            ) : (
              persons.map((person) => (
                <tr key={person.id}>
                  <td>{person.id}</td>
                  <td>{person.name}</td>
                  <td>{person.age}</td>
                  <td>
                    <button
                      className="action"
                      onClick={() => onEdit(person)}
                    >
                      Edit
                    </button>
                    <button
                      className="action danger"
                      onClick={() => onDelete(person.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default PersonTable;
