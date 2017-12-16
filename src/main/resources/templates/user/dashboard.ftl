<#include "*/header.ftl">

<div class="container">
    <div class="panel panel-default">
        <div class="panel-body">
            <nav class="navbar navbar-default ">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-sm-3">
                            <div class="navbar-header">
                                <a class="navbar-brand" href="#">Username</a>
                            </div>
                        </div>
                        <div class="col-sm-1"></div>
                        <div class="col-sm-6">
                            <div class="row">
                                <div class="float-xs-right">
                                    <form class="form-inline" action="/create" method="post">
                                        <div class="form-group">
                                            <input type="text" class="form-control" id="teamName" name="name"
                                                   placeholder="New Team name">
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
                            </div>
                        </div>
                        <div class="col-sm-2">
                            <div class="navbar-header">
                                <a class="navbar-brand" href="">Logout</a>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>
        </div>
    </div>
</div>
<div class="panel panel-default">
    <div class="panel-body">
        <div class="row">
            <div class="col-md-5 mt-1">
            <#list context.pages>
                <h2>Team List:</h2>
                <ul>
                    <#items as teamList1>
                        <li><a href="/wiki/${teamList1}">${teamList1}</a></li>
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
                        <li><a href="/wiki/${teamList2}">${teamList2}</a></li>
                    </#items>
                </ul>
            <#else>
                <p>Zero team.</p>
            </#list>
            </div>
        </div>
    </div>
</div>

<#include "*/footer.ftl">
