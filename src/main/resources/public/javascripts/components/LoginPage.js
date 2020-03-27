const security_question = "What is your school's name?";

class SignUpButton extends React.Component {
    render () {
        return (
            <button className="btn float-left text-white" onClick={this.props.callback}>SignUp</button>
        );
    }
}

class LoginButton extends React.Component {
    render () {
        return (
            <button className="btn float-left text-white" onClick={this.props.callback}>Login</button>
        );
    }
}

class LogoutButton extends React.Component {
    render () {
        return (
            <button className="btn float-left text-white" onClick={this.props.callback}>Logout</button>
        );
    }
}

class ForgotPassword extends React.Component{
    render () {
        return (
            <button className="btn float-left text-white" onClick={this.props.callback}>ForgotPassword</button>
        );
    }
}

class LoginPage extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {username:'', password:''};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(newState) {
        this._isMounted && this.setState(newState);
    }

    handleSubmit(event) {
        event.preventDefault();
        const formData = new FormData();
        formData.append("login", this.state.username);
        formData.append("password", this.state.password);
        formData.append("secQuestion", null);
        formData.append("secAns", null);
        formData.append("newPassword", null);

        fetch("/users/login", { method: "POST", body:formData })
            .then(response => {
                if(!response.ok) {
                    alert('Login Failed!');
                    this.props.callback({status:false,curUser:''});
                } else {
                    alert('Welcome back to Time Travel!');
                    this.props.callback({status:true,curUser:this.state.username});
                }
            });
        return false;
    }

    handleForgetPassClick(){

    }
    render() {
        return (

            <form onSubmit={this.handleSubmit}>
                <div className="form-group center">
                    <div className="title align-self-center text-center font-weight-bolder text-info">Login</div>
                    <FormLabel name="username" title="Email" required={true} type="email" handleChange={this.handleChange}/>
                    <FormLabel name="password" title="Password" required={true} type="password" handleChange={this.handleChange}/>
                    <input className="btn btn-success"  type="submit" value="Login" />
                    <button className="btn btn-success1" onClick={this.props.forgotPasswordCallback}>Forgot Password</button>


                </div>
            </form>
        );
    }
}

class SignUpPage extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {username:'', password:'', secAns:''};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(newState) {
        this._isMounted && this.setState(newState);
    }

    handleSubmit(event) {
        event.preventDefault();
        const formData = new FormData();
        formData.append("login", this.state.username);
        formData.append("password", this.state.password);
        formData.append("secQuestion", security_question);
        formData.append("secAns", this.state.secAns);
        fetch("/users", { method: "POST", body:formData })
            .then(response => {
                if(!response.ok) {
                    alert('Sign Up Failed!');
                    this.props.callback({status:false,curUser:''});
                } else {
                    alert('Welcome to Time Travel!');
                    alert('To view your travel plans, click Sync. \nIt syncs from your emails labeled as "oosetravel" starting from the first time sync is clicked. \nTo get travel plans from older emails, forward them to email address: "plans+029f81cb709843259f223dabcfc33a6b@in.us.traxo.com" and sync again. \n\nGo on and get started!');
                    this.props.callback({status:true,curUser:this.state.username});
                }
            });
        return false;
    }

    render() {
        return (
            <form onSubmit={this.handleSubmit}>

                <div className="form-group center2">
                    <div className="title align-self-center text-center font-weight-bolder text-info">SignUp</div>
                    <FormLabel name="username" title="Email" required={true} type="email" handleChange={this.handleChange}/>
                    <FormLabel name="password" title="Password" required={true} type="password" handleChange={this.handleChange}/>
                    <p>Security Question is {security_question}</p>
                    <FormLabel name="secAns" title="SecurityAnswer" required={true} type="text" handleChange={this.handleChange}/>
                    <input className="btn btn-success" type="submit" value="Sign Up" />
                </div>
                <div className="align-self-center center font-weight-lighter">*Enter email address that you will use to forward emails to the Traxo Mailbox.</div>
            </form>
        );
    }
}

class ForgotPasswordPage extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {username:'', secAns:'', newPassword:''};
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);

    }

    componentDidMount() {
        this._isMounted = true;
    }

    componentWillUnmount() {
        this._isMounted = false;
    }

    handleChange(newState) {
        this._isMounted && this.setState(newState);
    }

    handleSubmit(event) {
        event.preventDefault();
        const formData = new FormData();
        formData.append("login", this.state.username);
        formData.append("password", '');
        formData.append("newPassword", this.state.newPassword);
        formData.append("secQuestion", security_question);
        formData.append("secAns", this.state.secAns);
        fetch("/users/login", { method: "POST", body:formData })
            .then(response => {
                console.log(response);
                if(!response.ok) {
                    alert('Security Answer Incorrect! Try Again');
                    this.props.callback({status:false,curUser:''});
                } else {
                    alert('Welcome back to Time Travel!');
                    this.props.callback({status:true,curUser:this.state.username});
                }
            });
        return false;
    }

    handleForgetPassClick(){

    }
    render() {
        return (
            <form onSubmit={this.handleSubmit}>
                <div className="form-group center1">
                    <div className="title align-self-center text-center font-weight-bolder text-info">Forgot Password</div>
                    <FormLabel name="username" title="Email" required={true} type="email" handleChange={this.handleChange}/>
                    <p>Security Question is {security_question}</p>
                    <FormLabel name="secAns" title="Security Answer" required={true} type="password" handleChange={this.handleChange}/>
                    <FormLabel name="newPassword" title="New Password" required={true} type="password" handleChange={this.handleChange}/>
                    <input className="btn btn-success" type="submit" value="Submit" />


                </div>
            </form>
        );
    }
}
