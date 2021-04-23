import React from 'react';
import ProgressBar from 'react-bootstrap/ProgressBar'
import Popup from 'reactjs-popup';
import 'reactjs-popup/dist/index.css';

class StatInstrumentCandles extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      instrumentRefreshProgress: 0,
      instrumentRefreshCurrentMessage: "",
      isInstrumentPopupOpen: false,
      isInstrumentRefreshActive: false
    };

    this.refreshInstruments = this.refreshInstruments.bind(this);
    this.closeInstrumentPopup = this.closeInstrumentPopup.bind(this);
    this.pollRefresh = this.pollRefresh.bind(this);
  }

  sleep = timeoutMs => new Promise(resolve => setTimeout(resolve, timeoutMs))

  async pollRefresh() {
    while (this.state.isInstrumentRefreshActive) {
      await this.sleep(500);
      if (!this.state.isInstrumentRefreshActive) {
        break;
      } 
      fetch('http://localhost:8080/api/refresh/instruments/updatestatus')
      .then(response => response.json())
      .then(data => {
        console.log(data);
        this.setState({
          instrumentRefreshCurrentMessage: data.status,
          instrumentRefreshProgress: data.progress
        });
      })
      .catch(error => {
          console.log("Got error upon fetching of data: " + error);
      })
    }
  }

  async refreshInstruments() {
    const postRequestOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    };
    this.setState({
      instrumentRefreshCurrentMessage: "Refresh is initiated",
      isInstrumentPopupOpen: true,
      isInstrumentRefreshActive: true,
      instrumentRefreshProgress: 0
    }, this.pollRefresh); 
  
    fetch('http://localhost:8080/api/refresh/instruments', postRequestOptions)
    .then(response => response.text())
    .then(respText => {
      console.log(respText)
      this.setState({
          instrumentRefreshCurrentMessage: respText,
          isInstrumentPopupOpen: false,
          isInstrumentRefreshActive: false,
          instrumentRefreshProgress: 100
        })
    })
    .catch(error => {
        console.log("Got error upon fetching of data: " + error);
        this.setState({
          instrumentRefreshCurrentMessage: "Got error upon refresh: " + error,
          isInstrumentPopupOpen: false,
          isInstrumentRefreshActive: false,
          instrumentRefreshProgress: 100
        });
    })
  }

  closeInstrumentPopup() {
    this.setState({
      instrumentRefreshCurrentMessage: "",
      isInstrumentPopupOpen: false
    });
  }
  
  render() {
    return (
        <ul className="list-group">
          <li className="list-group-item d-flex justify-content-between align-items-center" id="total_instruments">
            Total instruments
            <span className="badge badge-primary badge-pill">{this.props.totalInstruments}</span>
            <button type="button" className="btn btn-outline-primary" onClick={this.refreshInstruments}>Refresh</button>
            <Popup open={this.state.isInstrumentPopupOpen} position="right center" onClose={this.closeInstrumentPopup} modal>
                <h6>{this.state.instrumentRefreshCurrentMessage}</h6>
              <ProgressBar now={this.state.instrumentRefreshProgress} label={`${this.state.instrumentRefreshProgress}%`}/>
            </Popup>
          </li>
          <li className="list-group-item d-flex justify-content-between align-items-center" id="total_candles">
            Total candles
            <span className="badge badge-primary badge-pill">{this.props.totalCandles}</span>
            <button type="button" className="btn btn-outline-primary">Refresh</button>
          </li>
        </ul>
    );
  }
}

export default StatInstrumentCandles;
