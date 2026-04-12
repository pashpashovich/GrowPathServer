package by.bsuir.growpathserver.common.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

import by.bsuir.growpathserver.common.web.GlobalRestExceptionHandler;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(GlobalRestExceptionHandler.class)
public class CommonWebAutoConfiguration {
}
