import React from 'react';


class NewInstruments extends React.Component {

    render() {
        if (this.props.instruments) {
            return (
                <table className="table table-striped">
                <tbody>
                    {this.props.instruments.map((instrument, index) => {
                        return (<tr key={index}>
                            <td key={index + "_1"}>{instrument.name}</td>
                            <td key={index + "_2"}>
                                <a href={"https://www.tinkoff.ru/invest/stocks/" + instrument.ticker}>{instrument.ticker}</a>
                            </td>
                        </tr>)
                    })}
                </tbody>
                </table>
            );
        } else return ("");
    }
}

export default NewInstruments;