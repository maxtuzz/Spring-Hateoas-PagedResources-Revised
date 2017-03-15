import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Iterator;
import java.util.List;

/**
 * PagedResources (because native Spring Hateoas PagedResources sucks :()
 * Page metadata wrapper
 *
 * Notes:
 *  - This will automatically add the following hypermedia links based on Page object:
 *      - FIRST
 *      - LAST
 *      - NEXT (if there is a next page)
 *      - PREVIOUS (if there is a previous)
 *      - SELF
 *  - It will also take an optional map of params (sort, size, and page params are automatically added if they are present)
 *      - This is useful if you are paginating a search for instance and need a link back like: /users?search=USERNAME&page=0&size=5&sort=name,asc
 *
 * @author Max Tuzzolino
 */

public class PagedResources<T> extends ResourceSupport implements Page<T> {
    private final Page<T> page;

    /**
     * Include just a page object if no query parameters are needed
     *
     * @param page - Our Page object
     */
    public PagedResources(Page<T> page) {
        super();
        this.page = page;

        addLinks(new LinkedMultiValueMap<>());
    }

    /**
     * Include both page and additional parameters
     *
     * @param page        - Out Page object
     * @param queryParams - MultiValueMap of additional parameters to include in links
     */
    public PagedResources(Page<T> page, MultiValueMap<String, String> queryParams) {
        super();
        this.page = page;

        addLinks(queryParams);
    }

    private ServletUriComponentsBuilder createBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri();
    }

    /**
     * Add links to our response
     * @param queryParams - MultiValueMap of additional parameters to include in links
     */
    private void addLinks(MultiValueMap<String, String> queryParams) {
        if (page.hasPrevious()) {
            Link link = buildPageLink(page.getNumber() - 1, page.getSize(), page.getSort(), Link.REL_PREVIOUS, queryParams);
            add(link);
        }

        if (page.hasNext()) {
            Link link = buildPageLink(page.getNumber() + 1, page.getSize(), page.getSort(), Link.REL_NEXT, queryParams);
            add(link);
        }

        Link link = buildPageLink(0, page.getSize(), page.getSort(), Link.REL_FIRST, queryParams);
        add(link);

        link = buildPageLink(page.getTotalPages() - 1, page.getSize(), page.getSort(), Link.REL_LAST, queryParams);
        add(link);

        link = buildPageLink(page.getNumber(), page.getSize(), page.getSort(), Link.REL_SELF, queryParams);
        add(link);
    }

    /**
     * Builds our link
     * @param page - Our page object
     * @param size - Number of resources
     * @param sort - String telling us what to sort by
     * @param rel - Link (SELF, FIRST, LAST, PREVIOUS, NEXT)
     * @param queryParams - Additional query parameters
     * @return A built link object
     */
    private Link buildPageLink(int page, int size, Sort sort, String rel, MultiValueMap<String, String> queryParams) {
        String path;

        MultiValueMap<String, String> pageParams = new LinkedMultiValueMap<>();

        // Add pageable query params
        pageParams.add("page", Integer.toString(page));
        pageParams.add("size", Integer.toString(size));

        // This is sort of messy
        if (sort != null) {
            String[] sortParams = sort.toString().split(":");
            sortParams[1] = sortParams[1].toLowerCase().trim();

            // Concat array into single string separated by comma
            pageParams.add("sort", String.join(",", sortParams));
        }

        path = createBuilder()
                .queryParams(queryParams)
                .queryParams(pageParams)
                .build()
                .toUriString();

        return new Link(path, rel);
    }

    public int getNumber() {
        return page.getNumber();
    }

    public int getSize() {
        return page.getSize();
    }

    public int getTotalPages() {
        return page.getTotalPages();
    }

    public int getNumberOfElements() {
        return page.getNumberOfElements();
    }

    public long getTotalElements() {
        return page.getTotalElements();
    }

    public <S> Page<S> map(Converter<? super T, ? extends S> converter) {
        return null;
    }

    public boolean hasPrevious() {
        return page.hasPrevious();
    }

    public Pageable nextPageable() {
        return null;
    }

    public Pageable previousPageable() {
        return null;
    }

    public boolean isFirst() {
        return page.isFirst();
    }

    public boolean hasNext() {
        return page.hasNext();
    }

    public boolean isLast() {
        return page.isLast();
    }

    public Iterator<T> iterator() {
        return page.iterator();
    }

    public List<T> getContent() {
        return page.getContent();
    }

    public boolean hasContent() {
        return page.hasContent();
    }

    public Sort getSort() {
        return page.getSort();
    }
}
