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
      candlesRefreshProgress: 0,
      candlesRefreshCurrentMessage: "",
      isCandlesPopupOpen: false,
      isCandlesRefreshActive: false,
      instrumentsFetched: [],
    };

    this.refreshInstruments = this.refreshInstruments.bind(this);
    this.refreshCandles = this.refreshCandles.bind(this);
    this.closeInstrumentPopup = this.closeInstrumentPopup.bind(this);
    this.pollInstrumentsRefresh = this.pollInstrumentsRefresh.bind(this);
    this.pollCandlesRefresh = this.pollCandlesRefresh.bind(this);
    this.closeInstrumentPopup = this.closeInstrumentPopup.bind(this);
    this.closeCandlesPopup = this.closeCandlesPopup.bind(this);
  }

  sleep = timeoutMs => new Promise(resolve => setTimeout(resolve, timeoutMs))

  async pollInstrumentsRefresh() {
    while (this.state.isInstrumentRefreshActive) {
      await this.sleep(500);
      if (!this.state.isInstrumentRefreshActive) {
        break;
      } 
      fetch('http://localhost:8080/refresh/instruments/updatestatus')
      .then(response => response.json())
      .then(data => {
        console.log(data);
        if (data.status) {
          this.setState({
            instrumentRefreshCurrentMessage: data.status
          });
        }
        this.setState({
          instrumentRefreshProgress: data.progress
        });
      })
      .catch(error => {
          console.log("Got error upon fetching of data: " + error);
      })
    }
  }

  async pollCandlesRefresh() {
    while (this.state.isCandlesRefreshActive) {
      await this.sleep(500);
      if (!this.state.isCandlesRefreshActive) {
        break;
      } 
      fetch('http://localhost:8080/refresh/candles/updatestatus')
      .then(response => response.json())
      .then(data => {
        console.log(data);
        if (data.status) {
          this.setState({
            candlesRefreshCurrentMessage: data.status
          });
        }
        this.setState({
          candlesRefreshProgress: data.progress
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
    }, this.pollInstrumentsRefresh); 
  
    fetch('http://localhost:8080/refresh/instruments', postRequestOptions)
    .then(response => response.json())
    .then(rspJson => {
      console.log(rspJson)
      
      if (rspJson.error) {
        this.setState({
          instrumentRefreshCurrentMessage: rspJson.error
        });
      } else {
        var message = rspJson.statusText;
        if (rspJson.newEntities && rspJson.newEntities.length > 0) {
          message += ", " + rspJson.newEntities.length + " new instrument(s) were added";
        }

        this.setState({
          instrumentRefreshCurrentMessage: message,
          instrumentsFetched: rspJson.newEntities,
          isInstrumentRefreshActive: false,
          instrumentRefreshProgress: 100
        })
      }  
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

  async refreshCandles() {
    const postRequestOptions = {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    };
    this.setState({
      candlesRefreshCurrentMessage: "Refresh is initiated",
      isCandlesPopupOpen: true,
      isCandlesRefreshActive: true,
      candlesRefreshProgress: 0
    }, this.pollCandlesRefresh); 
  
    fetch('http://localhost:8080/refresh/candles', postRequestOptions)
    .then(response => response.json())
    .then(rspJson => {
      console.log(rspJson)
      
      if (rspJson.error) {
        this.setState({
          candlesRefreshCurrentMessage: rspJson.error
        });
      } else {
        var message = rspJson.statusText;

        this.setState({
          candlesRefreshCurrentMessage: message,
          isCandlesRefreshActive: false,
          candlesRefreshProgress: 100
        })
      }  
    })
    .catch(error => {
        console.log("Got error upon fetching of candles refresh data: " + error);
        this.setState({
          candlesRefreshCurrentMessage: "Got error upon refresh: " + error,
          isCandlesPopupOpen: false,
          isCandlesRefreshActive: false,
          candlesRefreshProgress: 100
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

  closeCandlesPopup() {
    this.setState({
      isCandlesPopupOpen: false,
      isCandlesRefreshActive: false,
      candlesRefreshCurrentMessage: "",
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
            <button type="button" className="btn btn-outline-primary" onClick={this.refreshCandles}>Refresh</button>
            <Popup open={this.state.isCandlesPopupOpen}  contentStyle={{width: "20%"}} position="right center" className="text-center" onClose={this.closeCandlesPopup} modal>
              <div className="text-center">
                <h6>{this.state.candlesRefreshCurrentMessage}</h6>
              <ProgressBar now={this.state.candlesRefreshProgress} label={`${this.state.candlesRefreshProgress}%`}/>
              <button type="button" className="btn btn-sm mt-2 mb-1" onClick={this.closeCandlesPopup}>Close</button>
              </div>
            </Popup>
          </li>
        </ul>
    );
  }
}

export default StatInstrumentCandles;
