package de.ticketsystem.service;

import de.ticketsystem.domain.Category;
import de.ticketsystem.dto.CategoryResponse;
import de.ticketsystem.dto.CreateCategoryRequest;
import de.ticketsystem.exception.BusinessValidationException;
import de.ticketsystem.exception.ResourceNotFoundException;
import de.ticketsystem.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryOrThrow(id);
        return CategoryResponse.fromEntity(category);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessValidationException("Kategorie '" + request.name() + "' existiert bereits.");
        }

        Category category = new Category(request.name(), request.description());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    public Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kategorie mit ID " + id + " wurde nicht gefunden."));
    }
}
