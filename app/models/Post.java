package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MXBean;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;

import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Post extends Model {

	@Required
	public String title;
	
	@Required
	public Date postedAt;

	@Lob
	@Required
	@MaxSize(10000)
	public String content;

	@Required
	@ManyToOne
	public User author;

	@OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
	public List<Comment> comments;

	@ManyToMany(cascade = CascadeType.PERSIST)
	public Set<Tag> tags;

	public Post(String title, String content, User author) {
		this.title = title;
		this.content = content;
		this.author = author;
		this.postedAt = new Date();
		this.comments = new ArrayList<Comment>();
		this.tags = new TreeSet<Tag>();
	}

	public Post addComment(String author, String content) {
		Comment newComment = new Comment(author, content, this).save();
		this.comments.add(newComment);
		this.save();
		return this;
	}

	public Post tagItWith(String name) {
		tags.add(Tag.findOrCreateByName(name));
		return this;
	}

	public static List<Post> findTaggedWith(String... tags) {
		return Post
				.find("select distinct p from Post p join p.tags as t where t.name in (:tags) group by p.id, p.author, p.title, p.content,p.postedAt having count(t.id) = :size")
				.bind("tags", tags).bind("size", tags.length).fetch();
	}

	public Post previous() {
		return Post.find("postedAt < ? order by postedAt desc", postedAt)
				.first();
	}

	public Post next() {
		return Post.find("postedAt > ? order by postedAt asc", postedAt)
				.first();
	}
}
