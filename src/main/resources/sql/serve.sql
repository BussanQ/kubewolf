#define baseServeSelect()
select * from serve_task
#end

#sql("findByTaskName")
    #@baseServeSelect() where task_name=#para(0)
#end

#sql("deleteByTaskName")
    delete from serve_task where task_name=#para(0)
#end