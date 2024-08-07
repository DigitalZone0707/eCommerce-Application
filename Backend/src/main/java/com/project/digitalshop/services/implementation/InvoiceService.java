package com.project.digitalshop.services.implementation;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.project.digitalshop.dto.invoice.InvoiceDTO;
import com.project.digitalshop.dto.invoice.InvoiceUpdateDTO;
import com.project.digitalshop.dto.invoice.InvoiceUserDTO;
import com.project.digitalshop.dto.invoice.InvoiceResponseDTO;
import com.project.digitalshop.dto.category.CategoryProductDTO;
import com.project.digitalshop.dto.product.ProductResponseDTO;
import com.project.digitalshop.exception.NotFoundException;
import com.project.digitalshop.model.Invoice;
import com.project.digitalshop.model.Product;
import com.project.digitalshop.model.User;
import com.project.digitalshop.repository.InvoiceRepository;
import com.project.digitalshop.repository.ProductRepository;
import com.project.digitalshop.repository.UserRepository;
import com.project.digitalshop.services.interfaces.IInvoiceService;

import jakarta.validation.Valid;

@Service
@Validated
public class InvoiceService implements IInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository,
            ProductRepository productRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public InvoiceResponseDTO createInvoice(@Valid InvoiceDTO invoiceDTO) {
        Invoice invoice = new Invoice();
        User user = userRepository.findById(invoiceDTO.getUserId())
                .orElseThrow(() -> new NotFoundException("User Not Found!!!"));
        invoice.setUser(user);
        invoice.setProducts(invoiceDTO.getProductIds().stream()
                .map(productId -> productRepository.findById(productId)
                        .orElseThrow(() -> new NotFoundException("Product Not Found!!!")))
                .collect(Collectors.toList()));
        invoice.setSubTotal(invoiceDTO.getSubTotal());
        invoice.setTax(invoiceDTO.getTax());
        invoice.setTotalPrice(invoiceDTO.getTotalPrice());
        invoice = invoiceRepository.save(invoice);
        InvoiceResponseDTO invoiceResponseDTO = new InvoiceResponseDTO();
        BeanUtils.copyProperties(invoice, invoiceResponseDTO, "products", "user");
        InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
        BeanUtils.copyProperties(invoice.getUser(), invoiceUserDTO);
        invoiceResponseDTO.setUser(invoiceUserDTO);
        invoiceResponseDTO.setProducts(invoice.getProducts().stream().map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);

            // Map Category to CategoryDTO
            CategoryProductDTO categoryDTO = new CategoryProductDTO();
            BeanUtils.copyProperties(product.getCategory(), categoryDTO);
            productResponseDTO.setCategory(categoryDTO);
            return productResponseDTO;
        }).collect(Collectors.toList()));
        return invoiceResponseDTO;
    }

    @Override
    public InvoiceResponseDTO updateInvoice(UUID invoiceId, @Valid InvoiceUpdateDTO invoiceUpdateDTO) {
        Invoice existingInvoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice Not Found!!!"));
        existingInvoice.setPaymentStatus(invoiceUpdateDTO.getPaymentStatus());
        existingInvoice.setPaymentMethod(invoiceUpdateDTO.getPaymentMethod());
        invoiceRepository.save(existingInvoice);
        InvoiceResponseDTO invoiceResponseDTO = new InvoiceResponseDTO();
        BeanUtils.copyProperties(existingInvoice, invoiceResponseDTO, "products", "user");
        InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
        BeanUtils.copyProperties(existingInvoice.getUser(), invoiceUserDTO);
        invoiceResponseDTO.setUser(invoiceUserDTO);
        invoiceResponseDTO.setProducts(existingInvoice.getProducts().stream().map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);

            // Map Category to CategoryDTO
            CategoryProductDTO categoryDTO = new CategoryProductDTO();
            BeanUtils.copyProperties(product.getCategory(), categoryDTO);
            productResponseDTO.setCategory(categoryDTO);
            return productResponseDTO;
        }).collect(Collectors.toList()));
        return invoiceResponseDTO;
    }

    @Override
    public void deleteInvoice(UUID invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new NotFoundException("Invoice Not Found!!!");
        }
        invoiceRepository.deleteById(invoiceId);
    }

    @Override
    public InvoiceResponseDTO getInvoiceById(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new NotFoundException("Invoice Not Found!!!"));
        List<Product> products = invoice.getProducts();
        InvoiceResponseDTO invoiceResponseDTO = new InvoiceResponseDTO();
        BeanUtils.copyProperties(invoice, invoiceResponseDTO, "products", "user");
        InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
        BeanUtils.copyProperties(invoice.getUser(), invoiceUserDTO);
        invoiceResponseDTO.setUser(invoiceUserDTO);
        invoiceResponseDTO.setProducts(products.stream().map(product -> {
            ProductResponseDTO productResponseDTO = new ProductResponseDTO();
            BeanUtils.copyProperties(product, productResponseDTO);

            // Map Category to CategoryDTO
            CategoryProductDTO categoryDTO = new CategoryProductDTO();
            BeanUtils.copyProperties(product.getCategory(), categoryDTO);
            productResponseDTO.setCategory(categoryDTO);
            return productResponseDTO;
        }).collect(Collectors.toList()));
        return invoiceResponseDTO;
    }

    @Override
    public Page<InvoiceResponseDTO> getInvoicesByUserId(UUID userId, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Invoice> invoicePage = invoiceRepository.findAllByUserId(userId, pageable);

        return invoicePage.map(invoice -> {
            InvoiceResponseDTO invoiceResponseDTO = new InvoiceResponseDTO();
            BeanUtils.copyProperties(invoice, invoiceResponseDTO, "products", "user");
            InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
            BeanUtils.copyProperties(invoice.getUser(), invoiceUserDTO);
            invoiceResponseDTO.setUser(invoiceUserDTO);
            invoiceResponseDTO.setProducts(invoice.getProducts().stream().map(product -> {
                ProductResponseDTO productResponseDTO = new ProductResponseDTO();
                BeanUtils.copyProperties(product, productResponseDTO);

                // Map Category to CategoryDTO
                CategoryProductDTO categoryDTO = new CategoryProductDTO();
                BeanUtils.copyProperties(product.getCategory(), categoryDTO);
                productResponseDTO.setCategory(categoryDTO);
                return productResponseDTO;
            }).collect(Collectors.toList()));
            return invoiceResponseDTO;
        });
    }

    @Override
    public Page<InvoiceResponseDTO> getAllInvoices(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Invoice> invoicePage = invoiceRepository.findAll(pageable);

        return invoicePage.map(invoice -> {
            InvoiceResponseDTO invoiceResponseDTO = new InvoiceResponseDTO();
            BeanUtils.copyProperties(invoice, invoiceResponseDTO, "products", "user");
            InvoiceUserDTO invoiceUserDTO = new InvoiceUserDTO();
            BeanUtils.copyProperties(invoice.getUser(), invoiceUserDTO);
            invoiceResponseDTO.setUser(invoiceUserDTO);
            invoiceResponseDTO.setProducts(invoice.getProducts().stream().map(product -> {
                ProductResponseDTO productResponseDTO = new ProductResponseDTO();
                BeanUtils.copyProperties(product, productResponseDTO);

                // Map Category to CategoryDTO
                CategoryProductDTO categoryDTO = new CategoryProductDTO();
                BeanUtils.copyProperties(product.getCategory(), categoryDTO);
                productResponseDTO.setCategory(categoryDTO);
                return productResponseDTO;
            }).collect(Collectors.toList()));
            return invoiceResponseDTO;
        });
    }

}
