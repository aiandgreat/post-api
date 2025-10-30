package com.garcia.facebookapi;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Create a new post (returns 201 without a Location header)
    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post saved = postRepository.save(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @GetMapping
    public List<Post> listPosts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        if (page != null && size != null) {
            return postRepository.findAll(PageRequest.of(page, size)).getContent();
        } else {
            return postRepository.findAll();
        }
    }

    // Get a single post
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        Optional<Post> p = postRepository.findById(id);
        return p.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Full update (replace author/content/imageUrl)
    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestBody UpdatePostRequest req) {
        return postRepository.findById(id)
                .map(existing -> {
                    existing.setAuthor(req.author());
                    existing.setContent(req.content());
                    existing.setImageUrl(req.imageUrl());
                    Post updated = postRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Partial update - update any provided fields
    @PatchMapping("/{id}")
    public ResponseEntity<Post> patchPost(@PathVariable Long id, @RequestBody PatchPostRequest req) {
        return postRepository.findById(id)
                .map(existing -> {
                    if (req.author() != null) existing.setAuthor(req.author());
                    if (req.content() != null) existing.setContent(req.content());
                    if (req.imageUrl() != null) existing.setImageUrl(req.imageUrl());
                    Post updated = postRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Delete a post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        return postRepository.findById(id)
                .map(existing -> {
                    postRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DTOs (validation removed)

    public record CreatePostRequest(
            String author,
            String content,
            String imageUrl
    ) {}

    public record UpdatePostRequest(
            String author,
            String content,
            String imageUrl
    ) {}

    public record PatchPostRequest(
            String author,
            String content,
            String imageUrl
    ) {}
}