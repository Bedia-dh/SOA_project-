import React, { useState } from 'react';

function SearchBar({ onSearch, onClear }) {
  const [query, setQuery] = useState('');

  const handleSearch = () => {
    onSearch(query);
  };

  const handleClear = () => {
    setQuery('');
    onClear();
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <section className="card">
      <div className="card-head">
        <div>
          <h2>Search</h2>
          <p className="sub">Find persons by name or ID</p>
        </div>
      </div>
      <div className="search-form">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Enter name or ID..."
        />
        <button className="primary" onClick={handleSearch}>
          Search
        </button>
        <button className="ghost" onClick={handleClear}>
          Clear
        </button>
      </div>
    </section>
  );
}

export default SearchBar;
