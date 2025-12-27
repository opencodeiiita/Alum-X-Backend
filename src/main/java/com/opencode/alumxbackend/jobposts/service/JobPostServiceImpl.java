package com.opencode.alumxbackend.jobposts.service;

import com.opencode.alumxbackend.jobposts.dto.JobPostRequest;
import com.opencode.alumxbackend.jobposts.model.JobPost;
import com.opencode.alumxbackend.jobposts.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.opencode.alumxbackend.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import java.net.MalformedURLException;
import java.net.URL;
import com.opencode.alumxbackend.common.exception.BadRequestException;



// different from interface as here we are going to implement what we need
@Service
@RequiredArgsConstructor
public class JobPostServiceImpl implements JobPostService{
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;



    @Override
    public JobPost createJobPost(JobPostRequest request) {



        if(!userRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Username does not exist: " + request.getUsername());
        }

        if(request.getDescription().length()>5000 || request.getDescription().isBlank() || request.getDescription().length() < 50){
            throw new IllegalArgumentException("Description must be between 1 and 5000 characters");
        }

        if(request.getImageUrls()!=null  && !request.getImageUrls().isEmpty()){

//        if(request.getImageUrls().size()>5){
//            throw new IllegalArgumentException("Cannot upload more than 5 images");
//        }
            request.getImageUrls().forEach(url->{


                try{
                    new URL(url); // this will chck wether hte url is coorect
                }
                catch(MalformedURLException e){
                    throw new BadRequestException("invalid url: " + url);
                }
            });
        }
        JobPost jobPost = JobPost.builder()
                .postId(UUID.randomUUID().toString())
                .username(request.getUsername())
                .description(request.getDescription())
                .imageUrls(request.getImageUrls())
                .createdAt(LocalDateTime.now())
                .build();


        return jobPostRepository.save(jobPost);
    }
}
