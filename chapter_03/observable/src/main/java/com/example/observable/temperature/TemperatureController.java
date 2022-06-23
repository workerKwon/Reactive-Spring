package com.example.observable.temperature;

import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@RestController
public class TemperatureController {
    private final Set<SseEmitter> clients = new CopyOnWriteArraySet<>();

    @RequestMapping(value = "/temperature-stream", method = RequestMethod.GET)
    public SseEmitter events(HttpServletRequest request) {
        /**
         * TODO: Server-Sent Event. 클라이언트가 서버에서 자동으로 업데이트를 수신할 수 있다.
         * curl http://localhost:8080/temperature-stream
         */
        SseEmitter emitter = new SseEmitter();
        clients.add(emitter);

        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onCompletion(() -> clients.remove(emitter));
        return emitter;
    }

    @Async // 메소드를 비동기 실행으로 표시. 이 메소드는 별도로 구성된 스레드 풀에서 호출된다.
    @EventListener // 스프링으로부터 이벤트를 수신하기 위함
    public void handleMessage(Temperature temperature) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        clients.forEach(emitter -> {
            try {
                emitter.send(temperature, MediaType.APPLICATION_JSON);
            } catch (Exception ignore) {
                deadEmitters.add(emitter);
            }
        });
        clients.removeAll(deadEmitters);
    }
}
