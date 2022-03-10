import React from 'react';
import ProgressBar from 'react-bootstrap/ProgressBar'
import Popup from 'reactjs-popup';
import 'reactjs-popup/dist/index.css';
import NewInstruments from "./NewInstruments"

class StatInstrumentCandles extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      instrumentRefreshProgress: 0,
      instrumentRefreshCurrentMessage: "",
      isInstrumentPopupOpen: false,
      isInstrumentRefreshActive: false,
      instrumentsFetched: [],
    };

    this.refreshInstruments = this.refreshInstruments.bind(this);
    this.closeInstrumentPopup = this.closeInstrumentPopup.bind(this);
    this.pollRefresh = this.pollRefresh.bind(this);
    this.closeInstrumentPopup = this.closeInstrumentPopup.bind(this);
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
      instrumentRefreshProgress: 0,
      instrumentsFetched: []
    }, this.pollRefresh); 
  
    fetch('http://localhost:8080/api/refresh/instruments', postRequestOptions)
    .then(response => response.json())
    .then(rspJson => {
      console.log(rspJson)
      
      var tMessage = rspJson.mStatusText;
      if (rspJson.mNewEntities && rspJson.mNewEntities.length > 0) {
        tMessage += ", " + rspJson.mNewEntities.length + " new instrument(s) were added";
      }

      this.setState({
          instrumentRefreshCurrentMessage: tMessage,
          instrumentsFetched: rspJson.mNewEntities,
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
      isInstrumentPopupOpen: false,
      isInstrumentRefreshActive: false,
      instrumentRefreshCurrentMessage: "",
    });
  }
  
  render() {
    return (
        <ul className="list-group">
          <li className="list-group-item d-flex justify-content-between align-items-center" id="total_instruments">
            Total instruments
            <span className="badge badge-primary badge-pill">{this.props.totalInstruments}</span>
            <button type="button" className="btn btn-outline-primary" onClick={this.refreshInstruments}>Refresh</button>
            <Popup open={this.state.isInstrumentPopupOpen}  contentStyle={{width: "20%"}} position="right center" className="text-center" onClose={this.closeInstrumentPopup} modal>
              <div className="text-center">
                <h6>{this.state.instrumentRefreshCurrentMessage}</h6>
              <ProgressBar now={this.state.instrumentRefreshProgress} label={`${this.state.instrumentRefreshProgress}%`}/>
              <NewInstruments instruments={this.state.instrumentsFetched}/>
              <button type="button" className="btn btn-sm mt-2 mb-1" onClick={this.closeInstrumentPopup}>Close</button>
              </div>
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
