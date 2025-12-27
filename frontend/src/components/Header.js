import React from 'react';

function Header({ status }) {
  return (
    <header className="hero">
      <div>
        <p className="eyebrow">Projet SOA</p>
        <h1>Manage people with React + JAX-RS + JDBC</h1>
        <p className="lede">Fetch, create, update, and delete persons served by the Jersey API.</p>
      </div>
      <div className={`status ${status.isError ? 'error' : ''}`}>
        {status.message}
      </div>
    </header>
  );
}

export default Header;
