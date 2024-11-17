#include("serve.sql")
#define search(table,condition)
    select * from #(table) where 1=1
    #for(x : condition)
        #if(x.value)
        and #(x.key) #para(x.value)
        #end
    #end
#end

#define in(value)
in (
#for(x : values)
#para(x)#(for.last?'':',')
#end
)
#end

#sql("search")
    #@search(table,condition)
#end

#sql("searchOrder")
    #@search(table,condition) order by #(orderCol)
#end