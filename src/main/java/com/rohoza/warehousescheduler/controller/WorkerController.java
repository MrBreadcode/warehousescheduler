package com.rohoza.warehousescheduler.controller;

import com.rohoza.warehousescheduler.model.Worker;
import com.rohoza.warehousescheduler.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/workers")
public class WorkerController {

    private final WorkerService workerService;

    @Autowired
    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public String getAllWorkers(Model model) {
        List<Worker> workers = workerService.findAllSorted();
        model.addAttribute("workers", workers);
        return "worker-list";
    }

    @GetMapping("/add-worker")
    public String showCreateWorkerForm(Model model) {
        model.addAttribute("worker", new Worker());
        return "add-worker";
    }

    @PostMapping("/add-worker")
    public String createWorker(@ModelAttribute Worker worker) {
        workerService.save(worker);
        return "redirect:/api/workers";
    }

    @GetMapping("/edit-worker/{id}")
    public String showEditWorkerForm(@PathVariable Long id, Model model) {
        Worker worker = workerService.findById(id);
        model.addAttribute("worker", worker);
        return "edit-worker";
    }

    @PostMapping("/edit-worker/{id}")
    public String updateWorker(@PathVariable Long id, @ModelAttribute Worker worker) {
        worker.setId(id);
        workerService.save(worker);
        return "redirect:/api/workers";
    }

    @GetMapping("/delete-worker/{id}")
    public String deleteWorker(@PathVariable Long id) {
        workerService.deleteById(id);
        return "redirect:/api/workers";
    }
}
