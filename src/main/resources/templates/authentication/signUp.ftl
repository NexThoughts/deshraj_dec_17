<#include "*/header.ftl">
<div class="wrapper">
    <form action="" method="post" name="Login_Form" class="form-signin">
        <h3 class="form-signin-heading">Welcome Back! Please Sign In</h3>
        <hr class="colorgraph">
        <br>

        <input type="text" class="form-control" name="Username" placeholder="Username" required="" autofocus=""/>
        <input type="password" class="form-control" name="Password" placeholder="Password" required=""/>

        <div>
            <button class="btn btn-lg btn-primary btn-block" name="Reset" value="Rest" type="Submit">Reset</button>
            <button class="btn btn-lg btn-primary btn-block" name="Submit" value="Login" type="Submit">Login</button>
        </div>

    </form>
</div>
<#include "*/footer.ftl">
