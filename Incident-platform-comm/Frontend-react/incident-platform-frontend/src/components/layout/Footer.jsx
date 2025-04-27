import PropTypes from 'prop-types';

function Footer({ darkMode }) {
  const currentYear = new Date().getFullYear();
  
  return (
    <footer className={`app-footer ${darkMode ? 'dark' : ''}`}>
      <div className="footer-content">
        <div className="footer-copyright">
          <span>© {currentYear} Incident Platform. All rights reserved.</span>
        </div>
        
        <div className="footer-links">
          <a href="/privacy" className="footer-link">Privacy Policy</a>
          <a href="/terms" className="footer-link">Terms of Service</a>
          <a href="/help" className="footer-link">Help & Support</a>
        </div>
      </div>
    </footer>
  );
}

Footer.propTypes = {
  darkMode: PropTypes.bool
};

Footer.defaultProps = {
  darkMode: false
};

export default Footer; 