import com.example.chain.*;
import com.example.service.TaskService;
import com.example.model.AsyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 暂时禁用自动测试
//@Component
public class TestChainRunner implements CommandLineRunner {
    
    @Autowired
    private TaskHandlerFactory handlerFactory;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 测试责任链构建 ===");
        
        try {
            // 测试构建责任链
            TaskHandlerChain chain = handlerFactory.buildChain("VIDEO_SCORING");
            System.out.println("责任链构建成功，处理器数量: " + chain.getHandlerNames().size());
            System.out.println("处理器列表: " + chain.getHandlerNames());
            
            // 测试执行任务上下文
            TaskContext context = new TaskContext();
            context.setTaskId("TEST_001");
            context.setTaskType("VIDEO_SCORING");
            context.setData("videoId", "1001");
            
            System.out.println("开始执行任务...");
            HandleResult result = chain.execute(context);
            System.out.println("任务执行结果: " + (result.isSuccess() ? "成功" : "失败"));
            System.out.println("执行消息: " + result.getMessage());
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 测试完成 ===");
    }
}