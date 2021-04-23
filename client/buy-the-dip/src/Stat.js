import React from 'react';
import StatInstrumentCandles from './StatInstrumentCandles.js'
import { usePromiseTracker } from "react-promise-tracker"
import { trackPromise } from 'react-promise-tracker'
import Loader from 'react-loader-spinner'

const LoadingIndicator = props => {
     const { promiseInProgress } = usePromiseTracker();
     return (
       promiseInProgress && 
           <div
             style={{
               width: "100%",
               height: "100",
               display: "flex",
               justifyContent: "center",
               alignItems: "center"
             }} >
             <Loader type="ThreeDots" color="#0099ff" height="100" width="100" />
           </div>
    );  
}

class Stat extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      totalInstruments: 0,
      totalCandles: 0,
      candlesFreshness: []
    };
  }

  componentDidMount() {
    this.fetchOverallStat();
  }

  fetchOverallStat() {
    trackPromise(
      fetch('http://localhost:8080/api/statistics')
        .then(response => response.json())
        .then(response => {
            this.setState({
              totalInstruments: response.totalInstruments,
              totalCandles: response.totalCandles,
              candlesFreshness: Object.keys(response.candlesFreshness).map(
                function(keyName, keyIndex) {
                return(
                  <tr key={keyIndex}>
                    <td key="1">{keyName}</td>
                    <td key="2">{response.candlesFreshness[keyName]}</td>
                  </tr>
                )
                }
              )
            });
        })
        .catch(error => {
            console.log("Got error upon fetching of data: " + error);
            alert("Got error upon fetching of data: " + error);
        })
    );
}

  render() {
    return (
      <div>
        <div className="col-md-3">
          <h4 className="mt-3">Overall statistics</h4>
          <StatInstrumentCandles totalInstruments={this.state.totalInstruments} totalCandles={this.state.totalCandles}/>
        </div>
        <div className="col-md-2">
          <h4 className="mt-3">Candles freshness</h4>
          <LoadingIndicator/>
          <table id="dtBasicExample" className="table table-striped table-bordered table-sm" cellSpacing="0" width="50%">
            <thead>
              <tr>
                <th className="th-sm">Date</th>
                <th className="th-sm">Count</th>
              </tr>
            </thead>
            <tbody>
            {this.state.candlesFreshness}
            </tbody>
            <tfoot>
              <tr>
                <th className="th-sm">Date</th>
                <th className="th-sm">Count</th>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    );
  }
}

export default Stat;
