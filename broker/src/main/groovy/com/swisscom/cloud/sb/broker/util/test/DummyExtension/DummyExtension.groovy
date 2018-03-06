package com.swisscom.cloud.sb.broker.util.test.DummyExtension

import com.swisscom.cloud.sb.broker.async.job.JobManager
import com.swisscom.cloud.sb.broker.backup.shield.dto.TaskDto
import com.swisscom.cloud.sb.broker.cfextensions.extensions.Extension
import com.swisscom.cloud.sb.broker.cfextensions.extensions.ExtensionProvider
import org.springframework.beans.factory.annotation.Autowired

class DummyExtension implements ExtensionProvider{

    @Override
    TaskDto getTask(String taskUuid){
        return new TaskDto()
    }

    @Override
    Collection<Extension> buildExtensions(){
        return [new Extension(discovery_url: "DummyExtensionURL")]
    }

    String lockUser(String id){
        return "User locked with id = ${id}"
    }
}
