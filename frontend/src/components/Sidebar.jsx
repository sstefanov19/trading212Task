import { NavLink } from 'react-router-dom';
import './Sidebar.css';

function Sidebar({ onLogout }) {
  return (
    <aside className="sidebar">
      <div className="logo">
        <span className="logo-icon">📈</span>
        <span className="logo-text">TradingBot</span>
      </div>

      <nav className="nav-menu">
        <NavLink to="/" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
          <span className="nav-icon">📊</span>
          Dashboard
        </NavLink>
        <NavLink to="/trades" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
          <span className="nav-icon">📋</span>
          Trade History
        </NavLink>
        <NavLink to="/backtest" className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
          <span className="nav-icon">🧪</span>
          Backtest
        </NavLink>
      </nav>

      <div className="sidebar-footer">
        <button className="secondary logout-btn" onClick={onLogout}>Logout</button>
      </div>
    </aside>
  );
}

export default Sidebar;