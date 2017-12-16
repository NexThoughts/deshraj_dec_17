<#include "*/../header.ftl">
<div class="row">
    <div class="col-sm-3" style="background-color:lavender;"></div>
    <div class="col-sm-6">
        <div class="panel panel-default">
            <div class="panel-heading"><h2>NEW TASK</h2></div>
            <div class="panel-body">
                <div class="form-group">
                    <label>TASK NAME</label> <input type="taskName" class="form-control" placeholder="taskName">
                </div>
                <div>
                    <label>ASSIGNEE</label><input type="assignee" class="form-control" placeholder="assignee">
                </div>
                <div>
                    <label>DESCRIPTION</label><input type="desciption" class="form-control"
                                                     placeholder="desciption">
                </div>
                <div>
                    <label>USERS</label><input type="users" class="form-control" placeholder="users">
                </div>
                <div>
                    <label>ASSIGNEE</label><input type="assignee" class="form-control" placeholder="assignee">
                </div>

                <button type="submit" class="btn btn-default">Create</button>
            </div>
        </div>
    </div>
    <div class="col-sm-3" style="background-color:lavender;"></div>
</div>
<#include "*/../footer.ftl">
