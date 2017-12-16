<#include "*/header.ftl">

<nav class="navbar navbar-default ">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">Username</a>
        </div>
        <div class="navbar-header">
            <a class="navbar-brand" href="">Logout</a>
        </div>

    </div>
</nav>

<div class="row">
    <div class="col-md-12 mt-1">
        <div class="float-xs-right">
            <form class="form-inline" action="/create" method="post">
                <div class="form-group">
                    <input type="text" class="form-control" id="teamName" name="name" placeholder="New Team name">
                </div>
                <button type="submit" class="btn btn-primary">Create Team</button>
            </form>

            <!--<div class="float-xs-right">
                <form class="form-inline" action="/create" method="post">
                    <div class="form-group">
                        <input type="text" class="form-control" id="teamMemberName" name="name" placeholder="New User Name">
                    </div>
                    <button type="submit" class="btn btn-primary">Create User</button>
                </form>
            </div>-->
        </div>
        <h1 class="display-4">${context.title}</h1>
    </div>
</div>

<div class="row">
    <div class="col-md-5 mt-1">
    <#list context.pages>
        <h2>Members of Team:</h2>
        <ul>
            <#items as teamList1>
                <li><a href="/wiki/${page}">${teamList1}</a></li>
            </#items>
        </ul>
    <#else>
        <p>Zero team.</p>
    </#list>
    </div>

    <div class="col-md-2 mt-1"></div>

    <div class="col-md-5 mt-1">
    <#list context.pages>
        <h2>Owner Of Teams:</h2>
        <ul>
            <#items as teamList2>
                <li><a href="/wiki/${page}">${teamList2}</a></li>
            </#items>
        </ul>
    <#else>
        <p>Zero team.</p>
    </#list>
    </div>
</div>

<#include "*/footer.ftl">

