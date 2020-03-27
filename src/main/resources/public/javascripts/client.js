function get_sessionAttribute(sessionSet) {
    fetch("/users/login")
        .then(response => {
            if(!response.ok) {
                sessionSet(false)
            } else {
                sessionSet(true)
            }
        });
}
class Application extends React.Component {
    _isMounted = false;
    constructor(props) {
        super(props);
        this.state = {showNav:true, loggedIn:false, signupPage:false, curUser:'', forgotPassword:false};
        this.handleLogin = this.handleLogin.bind(this);
        this.handleSignUp = this.handleSignUp.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleLogout = this.handleLogout.bind(this);
        this.forgotPassword = this.forgotPassword.bind(this);
        this.resetForgotPassword = this.resetForgotPassword.bind(this);
        this.sessionSet = this.sessionSet.bind(this);
    }
    componentDidMount() {
        this._isMounted = true;
        if (localStorage.getItem('loggedIn')) {
            get_sessionAttribute(this.sessionSet)
        }
    }
    componentWillUnmount() {
        this._isMounted = false;
    }

    sessionSet(res) {
        if (res) {
            this._isMounted && this.setState({signupPage: false, loggedIn: true, curUser: localStorage.getItem('curUser')});
        }
    }

    handleSignUp() {
        localStorage.clear();
        this._isMounted && this.setState({signupPage:true, loggedIn:false, forgotPassword:false});
    }

    handleLogin() {
        localStorage.clear();
        this._isMounted && this.setState({signupPage:false, loggedIn:false, forgotPassword:false});
    }

    handleLogout() {
        localStorage.clear();
        this._isMounted && this.setState({signupPage:false, loggedIn:false});
    }

    handleSubmit(response) {
        if (response.status) {
            this._isMounted && this.setState({
                signupPage: false,
                loggedIn: true,
                curUser: response.curUser
            });
            localStorage.setItem('loggedIn', true);
            localStorage.setItem('curUser', response.curUser);
        }
    }

    forgotPassword() {
        this._isMounted && this.setState({forgotPassword:true})
    }
    resetForgotPassword() {
        this._isMounted && this.setState({forgotPassword:false})
    }
    render() {
        return (
            <div className="container-fluid">
                <div className="row bg-info font-weight-bolder text-light">
                    <div className="col-5 align-self-center ">
                        <Header/>
                    </div>
                    <div className="col-5 align-self-center">
                        {this.state.loggedIn?"Welcome "+this.state.curUser:""}
                    </div>
                    {this.state.loggedIn ?
                        <div className="col-2 align-self-center">
                            <div className="col-1 align-self-center">
                                <SyncEmailButton/>
                            </div>
                            <div className="col-1 align-self-center">
                                <LogoutButton callback={this.handleLogout}/>
                            </div>
                        </div>:
                        <div className="col-2 align-self-center">
                            <div className="col-1 align-self-center">
                                <SignUpButton callback={this.handleSignUp}/>
                            </div>
                            <div className="col-1 align-self-center">
                                <LoginButton callback={this.handleLogin}/>
                            </div>
                        </div>

                    }
                </div>
                <div className="row">
                    {this.state.loggedIn && this.state.curUser?
                        <HomePageTabs/>
                        :this.state.signupPage?
                            <SignUpPage callback={this.handleSubmit}/>
                            :this.state.forgotPassword?
                                <ForgotPasswordPage resetForgotPassword={this.resetForgotPassword} callback={this.handleSubmit}/>
                                :<LoginPage callback={this.handleSubmit} forgotPasswordCallback={this.forgotPassword}/>}
                </div>

            </div>
    );
    }
}

class Loader extends React.Component{
    render(){
        return(<div style={{backgroundColor:'#FAF8F9', width:'100%', height:'100vh'}}>
            <img src={'/images/loader2.gif'} style={{display:'block',marginLeft:'auto', marginRight:'auto'}}/>
        </div>)
    }
}

class MainApplication extends React.Component{
    constructor(props){
        super(props);
        this.state = {timer:true};
        this.changeState = this.changeState.bind(this);
    }

    changeState(){
        this.setState({timer:false});
    }

    render(){
        setTimeout(() => {this.changeState();}, 3000);
        return(
            this.state.timer?<Loader/>:<Application/>
        );
    }
}

const Header = () => (
    <div>
        TIME TRAVEL<br/>
        <small>A <a className="link text-light" href="https://github.com/jhu-oose/2019-group-IndiPoltergeists">Time Travel Application</a> for <a className="link text-light" href="https://www.jhu-oose.com">OOSE</a></small>
    </div>
);

ReactDOM.render(<MainApplication/>, document.querySelector("#application"));

