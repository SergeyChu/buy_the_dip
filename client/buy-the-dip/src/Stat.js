import React from 'react';
import StatInstrumentCandles from './StatInstrumentCandles.js'
import { usePromiseTracker } from "react-promise-tracker"
import { trackPromise } from 'react-promise-tracker'
import Loader from 'react-loader-spinner'
import 'react-bootstrap-table-next/dist/react-bootstrap-table2.min.css';
import BootstrapTable from 'react-bootstrap-table-next';


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
      candlesFreshness: [],
      instrumentsFreshness: [],
      columns: [{
        dataField: 'date',
        text: 'Date',
        sort: true
      }, {
        dataField: 'count',
        text: 'Count',
        sort: true
      }]
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
              candlesFreshness: response.candlesFreshness,
              instrumentsFreshness: response.instrumentsFreshness
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
        <div className="col-md-5">
          <h4 className="mt-3">Overall statistics</h4>
          <StatInstrumentCandles totalInstruments={this.state.totalInstruments} totalCandles={this.state.totalCandles}/>
        </div>
        <div class="row ml-1">
          <div className="col-md-3">
            <h6 className="mt-3">Latest candles dates per instrument overview</h6>
            <LoadingIndicator/>
            <BootstrapTable keyField='id' data={ this.state.candlesFreshness } columns={ this.state.columns } />
          </div>
          <div className="col-md-3">
            <h6 className="mt-3">Instruments creation dates overview</h6>
            <LoadingIndicator/>
            <BootstrapTable keyField='id' data={ this.state.instrumentsFreshness } columns={ this.state.columns } />
          </div>
        </div>
      </div>
    );
  }
}

export default Stat;
