class TabButton extends React.Component {
    render () {
        return (<button className={`btn btn-block text-sm-left ${this.props.active}`} onClick={this.props.handleClick}>{this.props.title}</button>);
    }
}

class MyLabel extends React.Component {

    get_pattern_placeholder() {
        if (this.props.name==="checkin_date" || this.props.name==="checkout_date" || this.props.name==="departure_date" || this.props.name==="arrival_date" || this.props.name==="created_date") {
            return ["^(19|20)\\d\\d-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$", "YYYY-MM-DD"];
        } else if(this.props.name==="departure_time" || this.props.name==="arrival_time" || this.props.name==="created_time"){
            return ["^(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$", "HH:MM:SS"];
        }
        return [null, null];
    }

    render () {
        let [pattern, placeholder] = this.get_pattern_placeholder();
        return (
            <label className="row">
                <div className="col-4">
                {this.props.title}{this.props.required?'*':''}:
                </div>
                <div className="col">
                <input className="form-control" required={this.props.required} pattern={pattern} placeholder={placeholder} type={this.props.type} name={this.props.name} value={this.props.value} onChange={this.props.handleChange} />
                </div>
            </label>
        );
    }
}

class FormLabel extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {
            value:''
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(event) {
        this._isMounted && this.setState({value: event.target.value});

        let returnObj={};
        returnObj[this.props.name]=event.target.value;
        this.props.handleChange(returnObj);
    }

    render () {
        let input_type=this.props.type?this.props.type:"text";
        return(
            <MyLabel title={this.props.title} type={input_type}  name={this.props.name} value={this.state.value} required={this.props.required} handleChange={this.handleChange}/>
        );
    }
}

class FormDynamicLabel extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {
            value:''
        };
        this.handleChange = this.handleChange.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(event) {
        this._isMounted && this.setState({value: event.target.value});
        this.props.handleChange(event.target.value,this.props.index);
    }

    render () {
        var traveler_num = this.props.index + 1;
        return(
            <MyLabel title={this.props.title+" "+traveler_num} type={"text"}  name={this.props.name} value={this.state.value} required={this.props.required} handleChange={this.handleChange}/>
        );
    }
}

