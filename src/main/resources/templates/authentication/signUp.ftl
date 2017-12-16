<#include "*/header.ftl">
<section>
    <div class="container">
        <div class="row text-center">
            <h2 class="title" style="opacity: 0;margin-top: 45px">Sign Up</h2>
        </div>
        <div class="row text-center" style="margin-top: 50px;margin-left:200px;">
            <div class="contact-form col-md-6 col-sm-12 col-xs-12 col-md-offset-3">
                <form class="form" method="POST">

                    <label class="sr-only" for="email">Username</label>
                    <input id="email" name="email" type="email" class="form-control" placeholder="Username:"
                           required>

                    <label class="sr-only" for="password">Password</label>
                    <input id="password" type="password" name="password" class="form-control" placeholder="Password :"
                           required>

                    <label class="sr-only" for="confirmPassword">Password</label>
                    <input id="confirmPassword" type="password" name="confirmPassword" class="form-control"
                           placeholder="Confirm Password :"
                           required>

                    <button type="submit" class="btn btn-lg btn-theme">SignUp</button>
                </form>
            </div>
        </div>
    </div>
</section>

<#include "*/footer.ftl">
