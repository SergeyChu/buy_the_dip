import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min';
import React from 'react';


class Menu extends React.Component {
  render() {
    return (     
      <nav className="navbar navbar-expand-lg navbar-light bg-light">
        <a className="navbar-brand" href="#top">BuyTheDip</a>
        <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavAltMarkup" aria-controls="navbarNavAltMarkup" aria-expanded="false" aria-label="Toggle navigation">
          <span className="navbar-toggler-icon"></span>
        </button>
        <div className="collapse navbar-collapse" id="navbarNavAltMarkup">
          <div className="navbar-nav">
            <a className="nav-item nav-link active" href="#top">Statistics <span className="sr-only">(current)</span></a>
            <a className="nav-item nav-link" href="#top">Daily dip</a>
            <a className="nav-item nav-link" href="#top">Custom dip</a>
            <a className="nav-item nav-link disabled" href="#top">Watchlist</a>
          </div>
        </div>
      </nav>    
    )
  }
}

export default Menu;
