package com.falong.ppmtool.services;

import com.falong.ppmtool.domain.Backlog;
import com.falong.ppmtool.domain.Project;
import com.falong.ppmtool.domain.ProjectTask;
import com.falong.ppmtool.exceptions.ProjectIdException;
import com.falong.ppmtool.exceptions.ProjectNotFoundException;
import com.falong.ppmtool.repositories.BacklogRepository;
import com.falong.ppmtool.repositories.ProjectRepository;
import com.falong.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService {
    @Autowired
    private BacklogRepository backlogRepository;
    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask){
        try{
            // exception: project not found
            //project tasks to be added to a specific project, project != null, BL exists.
            Backlog backlog = backlogRepository.findByProjectIdentifier(projectIdentifier);

            // set the backlog to the project task
            projectTask.setBacklog(backlog);
            // we ant our project swquence to be like this: IDPRO-1, IDPRO-2...
            Integer BacklogSequence = backlog.getPTSequence();
            // update the backlog sequence
            BacklogSequence++;
            backlog.setPTSequence(BacklogSequence);
            //add sequence to the task
            projectTask.setProjectSequence(projectIdentifier+"-"+BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);
            // intital priority when priority is null
            if (projectTask.getPriority() == 0 || projectTask.getPriority() == null) {
                projectTask.setPriority(3);
            }
            // initial status when status is null
            if (projectTask.getStatus() == null || projectTask.getStatus() == "") {
                projectTask.setStatus("TO_DO");
            }
            return projectTaskRepository.save(projectTask);
        }catch(Exception e) {
            throw new ProjectNotFoundException("Project not Found!");
        }

    }

    public Iterable<ProjectTask> finBacklogById(String id) {
        Project project = projectRepository.findByProjectIdentifier(id);

        if (project == null) {
            throw new ProjectNotFoundException("Project with ID" + id + " doesn't exist");
        }

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(id);
    }

    public ProjectTask findPTByProjectSequence (String backlog_id, String pt_id) {
        // make sure we are searching on the right backlog
        Backlog backlog = backlogRepository.findByProjectIdentifier(backlog_id);
        if (backlog == null) {
            throw new ProjectNotFoundException("Project with ID " + backlog_id + " doesn't exist");
        }

        //make sure that our task exists
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask == null) {
            throw new ProjectNotFoundException("task with sequence " + pt_id + " doesn't exist");
        }

        //make sure that the backlog/project id in the path corresponds to the right project
        if (!projectTask.getProjectIdentifier().equals(backlog_id)) {
            throw new ProjectNotFoundException("Project Task " + pt_id + " doesn't exist in project " + backlog_id);
        }

        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id) {
        ProjectTask projectTask = findPTByProjectSequence(backlog_id,pt_id);

        projectTask = updatedTask;
        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence( String backlog_id, String pt_id) {
        ProjectTask projectTask = findPTByProjectSequence(backlog_id,pt_id);

//        Backlog backlog = projectTask.getBacklog();
//        List<ProjectTask> pts = backlog.getProjectTasks();
//        pts.remove(projectTask);
//        backlogRepository.save(backlog);
        projectTaskRepository.delete(projectTask);
    }
}
